package servico;

import com.fasterxml.jackson.core.JsonProcessingException;
import dao.LocalizacaoDao;
import dao.PrevisaoDao;
import jakarta.persistence.PersistenceException;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ColetaService {

    private static final Logger LOG = Logger.getLogger(ColetaService.class.getName());
    private static final int LIMITE_ALERTA = 5;   // falhas seguidas até o alerta forte

    private final ClienteClima client;
    private final LocalizacaoDao localizacaoDao;
    private final PrevisaoDao previsaoDao;
    private final boolean pularRepetidos;

    // cidade -> quantas falhas seguidas
    private final Map<String, Integer> falhasSeguidas = new ConcurrentHashMap<>();

    public ColetaService(ClienteClima client, LocalizacaoDao localizacaoDao,
                         PrevisaoDao previsaoDao, boolean pularRepetidos) {
        this.client = client;
        this.localizacaoDao = localizacaoDao;
        this.previsaoDao = previsaoDao;
        this.pularRepetidos = pularRepetidos;
    }

    public List<PrevisaoDoCLima> coletar(List<Localizacao> cidades) {
        List<PrevisaoDoCLima> gravadas = new ArrayList<>();

        for (Localizacao cidade : cidades) {
            String nome = cidade.getCity();
            try {
                Localizacao local = garantirLocalizacao(cidade);
                PrevisaoDoCLima previsao = client.buscar(local);

                if (pularRepetidos && previsaoDao.existe(local, previsao.getForecastAt())) {
                    LOG.info(nome + ": sem dado novo da API, não gravado");
                } else {
                    previsaoDao.salvar(previsao);
                    gravadas.add(previsao);
                }
                registrarSucesso(nome);

            } catch (ConnectException | HttpTimeoutException e) {
                registrarFalha(nome, "sem conexão com a API ou tempo esgotado", null);
            } catch (ApiException e) {
                registrarFalha(nome, "API respondeu com status " + e.getStatus()
                        + ": " + resumo(e.getCorpo()), null);
            } catch (DadoInvalidoException | JsonProcessingException e) {
                registrarFalha(nome, "dados inválidos recebidos: " + e.getMessage(), null);
            } catch (PersistenceException e) {
                registrarFalha(nome, "erro no banco de dados", e);      // com rastro completo
            } catch (Exception e) {
                registrarFalha(nome, "erro inesperado", e);             // com rastro completo
            }
        }
        return gravadas;
    }

    private Localizacao garantirLocalizacao(Localizacao cidade) {
        return localizacaoDao.buscarPorCidade(cidade.getCity()).orElseGet(() -> {
            localizacaoDao.salvar(cidade);
            return cidade;
        });
    }

    private void registrarSucesso(String cidade) {
        Integer antes = falhasSeguidas.remove(cidade);
        if (antes != null) {
            LOG.info(cidade + ": coleta normalizada após " + antes + " falha(s) seguida(s)");
        }
    }

    private void registrarFalha(String cidade, String motivo, Throwable causa) {
        int total = falhasSeguidas.merge(cidade, 1, Integer::sum);

        String mensagem = cidade + ": " + motivo + " (falha " + total + " seguida(s))";
        Level nivel = Level.WARNING;
        if (total == LIMITE_ALERTA) {
            nivel = Level.SEVERE;
            mensagem += " - FALHA PERSISTENTE";
        }

        if (causa != null) {
            LOG.log(nivel, mensagem, causa);
        } else {
            LOG.log(nivel, mensagem);
        }
    }

    private static String resumo(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() > 200 ? texto.substring(0, 200) + "..." : texto;
    }
}