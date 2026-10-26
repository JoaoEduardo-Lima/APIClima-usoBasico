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

    public ColetaService(ClienteClima client, LocalizacaoDao localizacaoDao, PrevisaoDao previsaoDao) {
        this.client = client;
        this.localizacaoDao = localizacaoDao;
        this.previsaoDao = previsaoDao;
    }

    /** Coleta e grava a previsão de cada cidade. Devolve o que foi gravado. */
    public List<PrevisaoDoCLima> coletar(List<Localizacao> cidades) {
        List<PrevisaoDoCLima> gravadas = new ArrayList<>();

        for (Localizacao cidade : cidades) {
            try {
                Localizacao local = garantirLocalizacao(cidade);
                PrevisaoDoCLima previsao = client.buscar(local);
                previsaoDao.salvar(previsao);
                gravadas.add(previsao);
            } catch (Exception e) {
                // uma cidade com problema não derruba as outras
                LOG.log(Level.WARNING, "Falha ao coletar " + cidade.getCity(), e);
            }
        }
        return gravadas;
    }

    // usa a cidade que já está no banco; se não existir, cadastra
    private Localizacao garantirLocalizacao(Localizacao cidade) {
        return localizacaoDao.buscarPorCidade(cidade.getCity()).orElseGet(() -> {
            localizacaoDao.salvar(cidade);   // depois do salvar, a cidade passa a ter id
            return cidade;
        });
    }
}