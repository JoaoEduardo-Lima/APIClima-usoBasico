package app;

import apresentacao.ApresentadorClima;
import config.HibernateUtil;
import dao.LocalizacaoDao;
import dao.LocalizacaoDaoHibernate;
import dao.PrevisaoDao;
import dao.PrevisaoDaoHibernate;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;
import servico.ColetaService;
import servico.ClienteClima;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final long INTERVALO_SEGUNDOS = 10;

    public static void main(String[] args) {
        List<Localizacao> cidades = List.of(
                new Localizacao("São Paulo", -23.55, -46.63, "America/Sao_Paulo"),
                new Localizacao("Rio de Janeiro", -22.91, -43.17, "America/Sao_Paulo"),
                new Localizacao("Brasília", -15.79, -47.88, "America/Sao_Paulo"),
                new Localizacao("Salvador", -12.97, -38.50, "America/Sao_Paulo"),
                new Localizacao("Porto Alegre", -30.03, -51.23, "America/Sao_Paulo"));

        LocalizacaoDao localizacaoDao = new LocalizacaoDaoHibernate(HibernateUtil.getSessionFactory());
        PrevisaoDao previsaoDao = new PrevisaoDaoHibernate(HibernateUtil.getSessionFactory());
        ClienteClima client = new ClienteClima();
        ColetaService coleta = new ColetaService(client, localizacaoDao, previsaoDao);
        ApresentadorClima presenter = new ApresentadorClima();

        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();

        Runnable tarefa = () -> {
            try {
                LOG.info("Iniciando coleta...");
                for (PrevisaoDoCLima p : coleta.coletar(cidades)) {
                    presenter.exibir(p);
                }
            } catch (Exception e) {
                // sem este catch, uma exceção cancela TODAS as próximas execuções, em silêncio
                LOG.log(Level.SEVERE, "Erro inesperado na coleta", e);
            }
        };

        // primeira coleta imediata; depois, 10 s após o FIM de cada coleta
        agendador.scheduleWithFixedDelay(tarefa, 0, INTERVALO_SEGUNDOS, TimeUnit.SECONDS);

        // roda quando o programa é encerrado (Ctrl+C, por exemplo)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Encerrando o coletor...");
            agendador.shutdown();
            try {
                if (!agendador.awaitTermination(15, TimeUnit.SECONDS)) {
                    agendador.shutdownNow();
                }
            } catch (InterruptedException e) {
                agendador.shutdownNow();
                Thread.currentThread().interrupt();
            }
            client.close();
            HibernateUtil.fechar();
        }));
    }
}