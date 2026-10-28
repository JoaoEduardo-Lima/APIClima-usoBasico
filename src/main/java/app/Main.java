package app;

import apresentacao.ApresentadorClima;
import config.HibernateUtil;
import dao.LocalizacaoDao;
import dao.LocalizacaoDaoHibernate;
import dao.PrevisaoDao;
import dao.PrevisaoDaoHibernate;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;
import servico.ClienteClima;
import servico.ColetaService;
import servico.FonteDeClima;
import simulacao.Cenario;
import simulacao.ClienteSimulado;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    private static final long INTERVALO_SEGUNDOS = 10;
    private static final boolean PULAR_REPETIDOS = true;   // false = grava a cada 10 s, mesmo repetido

    public static void main(String[] args) {
        // argumentos: --simulacao  e  --cenario=tempestade
        boolean simulacao = false;
        Cenario cenario = Cenario.DIA_CALMO;
        for (String arg : args) {
            if (arg.equals("--simulacao")) {
                simulacao = true;
            } else if (arg.startsWith("--cenario=")) {
                cenario = Cenario.deTexto(arg.substring("--cenario=".length()));
            }
        }

        FonteDeClima fonte;
        if (simulacao) {
            fonte = new ClienteSimulado(cenario);
            LOG.warning("MODO SIMULAÇÃO ativo. Cenário: " + cenario + ". Os dados NÃO são reais.");
        } else {
            fonte = new ClienteClima();
            LOG.info("Modo real: coletando da Open-Meteo.");
        }

        List<Localizacao> cidades = List.of(
                new Localizacao("São Paulo", -23.55, -46.63, "America/Sao_Paulo"),
                new Localizacao("Rio de Janeiro", -22.91, -43.17, "America/Sao_Paulo"),
                new Localizacao("Brasília", -15.79, -47.88, "America/Sao_Paulo"),
                new Localizacao("Salvador", -12.97, -38.50, "America/Sao_Paulo"),
                new Localizacao("Porto Alegre", -30.03, -51.23, "America/Sao_Paulo"));

        LocalizacaoDao localizacaoDao = new LocalizacaoDaoHibernate(HibernateUtil.getSessionFactory());
        PrevisaoDao previsaoDao = new PrevisaoDaoHibernate(HibernateUtil.getSessionFactory());
        ColetaService coleta = new ColetaService(fonte, localizacaoDao, previsaoDao, PULAR_REPETIDOS);
        ApresentadorClima presenter = new ApresentadorClima();

        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();

        Runnable tarefa = () -> {
            try {
                LOG.info("Iniciando coleta...");
                for (PrevisaoDoCLima p : coleta.coletar(cidades)) {
                    presenter.exibir(p);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Erro inesperado na coleta", e);
            }
        };

        agendador.scheduleWithFixedDelay(tarefa, 0, INTERVALO_SEGUNDOS, TimeUnit.SECONDS);

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
            fonte.close();
            HibernateUtil.fechar();
        }));
    }
}