package servico;

import dao.LocalizacaoDao;
import dao.PrevisaoDao;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ColetaService {

    private static final Logger LOG = Logger.getLogger(ColetaService.class.getName());

    private final ClienteClima client;
    private final LocalizacaoDao localizacaoDao;
    private final PrevisaoDao previsaoDao;
    private final boolean pularRepetidos;

    public ColetaService(ClienteClima client, LocalizacaoDao localizacaoDao,
                         PrevisaoDao previsaoDao, boolean pularRepetidos) {
        this.client = client;
        this.localizacaoDao = localizacaoDao;
        this.previsaoDao = previsaoDao;
        this.pularRepetidos = pularRepetidos;
    }

    /** Coleta e grava a previsão de cada cidade. Devolve só o que foi gravado de fato. */
    public List<PrevisaoDoCLima> coletar(List<Localizacao> cidades) {
        List<PrevisaoDoCLima> gravadas = new ArrayList<>();

        for (Localizacao cidade : cidades) {
            try {
                Localizacao local = garantirLocalizacao(cidade);
                PrevisaoDoCLima previsao = client.buscar(local);

                if (pularRepetidos && previsaoDao.existe(local, previsao.getForecastAt())) {
                    LOG.info(cidade.getCity() + ": sem dado novo da API, não gravado");
                    continue;
                }

                previsaoDao.salvar(previsao);
                gravadas.add(previsao);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Falha ao coletar " + cidade.getCity(), e);
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
}