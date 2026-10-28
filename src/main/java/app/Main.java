package app;
//Classe Responsavel por exibir os dados de clima
import apresentacao.ApresentadorClima;

//Utilitario q gerencia a abertura e o fechamento da conexão
import config.HibernateUtil;

//Acesso de dados da Localizacao
import dao.LocalizacaoDao;
import dao.LocalizacaoDaoHibernate;

//Acesso de dados da previsao temporal
import dao.PrevisaoDao;
import dao.PrevisaoDaoHibernate;

//Entidades dominio
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

//Implementação que busca os dados da API
import servico.ClienteClima;

//Realiza a coleta (chama a fonte + persiste no banco)
import servico.ColetaService;

//interface comum as fontes tanto a real quanto a simulada
import servico.FonteDeClima;

//enum com os cenarios simulados
import simulacao.Cenario;

//fonte q gera os dados simulados
import simulacao.ClienteSimulado;

// lista imutável das cidades
import java.util.List;

//Ferramentas para a criação de um agendador de tarefas periodicas
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//API de log padrão do java
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    //Cria o logger nomeado com o FQN da classe, usado para registrar mensagens.
    private static final Logger LOG = Logger.getLogger(Main.class.getName());
    //intervalo de tempo entre coletas de dados
    private static final long INTERVALO_SEGUNDOS = 10;
    //flag responsavel por não armazenar dados identicas a anterior para evitar lixo
    private static final boolean PULAR_REPETIDOS = true;   // false = grava a cada 10 s, mesmo repetido

    //Ponto de entrada do programa
    public static void main(String[] args) {
        // argumentos: --simulacao  e  --cenario=tempestade
        boolean simulacao = false; // True caso queira testar a simulação
        Cenario cenario = Cenario.DIA_CALMO; // cenario default
        // coleta os argumentos da linha de comando
        for (String arg : args) {
            if (arg.equals("--simulacao")) { // ativa a simulação
                simulacao = true;
            } else if (arg.startsWith("--cenario=")) { // extrai o texto após = e cpnverta para o respectivo enum
                cenario = Cenario.deTexto(arg.substring("--cenario=".length()));
            }
        }

        //Define a escolha de fonte
        FonteDeClima fonte;
        if (simulacao) {
            fonte = new ClienteSimulado(cenario); // Se simulação estiver como true então recebe os dados do Cliente simulação
            LOG.warning("MODO SIMULAÇÃO ativo. Cenário: " + cenario + ". Os dados NÃO são reais.");
        } else {
            fonte = new ClienteClima(); // Caso o contrario retorna para o default de receber da API
            LOG.info("Modo real: coletando da Open-Meteo.");
        }

        //Lista imutável de cidades q são coletadas a cada ciclo
        List<Localizacao> cidades = List.of(
                new Localizacao("São Paulo", -23.55, -46.63, "America/Sao_Paulo"),
                new Localizacao("Rio de Janeiro", -22.91, -43.17, "America/Sao_Paulo"),
                new Localizacao("Brasília", -15.79, -47.88, "America/Sao_Paulo"),
                new Localizacao("Salvador", -12.97, -38.50, "America/Sao_Paulo"),
                new Localizacao("Porto Alegre", -30.03, -51.23, "America/Sao_Paulo"));

        //Montagem das dependências
        //Instância os DAOs concretos
        LocalizacaoDao localizacaoDao = new LocalizacaoDaoHibernate(HibernateUtil.getSessionFactory());
        PrevisaoDao previsaoDao = new PrevisaoDaoHibernate(HibernateUtil.getSessionFactory());
        //Cria o serviço que organiza: (Fonte -> DAO de localização -> Dao previsao) respeitando a flag de pular repetido
        ColetaService coleta = new ColetaService(fonte, localizacaoDao, previsaoDao, PULAR_REPETIDOS);
        //Cria o objeto q formata e imprime os dados coletados
        ApresentadorClima presenter = new ApresentadorClima();

        //Cria o executor para tarefas agendadas
        ScheduledExecutorService agendador = Executors.newSingleThreadScheduledExecutor();

        //Define o runnable que loga o início
        Runnable tarefa = () -> {
            try {
                LOG.info("Iniciando coleta...");
                //Chama o coleta.coletar(cidades)
                for (PrevisaoDoCLima p : coleta.coletar(cidades)) {
                    //Atraves do presenter demonstra a previsao
                    presenter.exibir(p);
                }
            } catch (Exception e) { //Captura qualquer exceção para n travar o agendador
                LOG.log(Level.SEVERE, "Erro inesperado na coleta", e);
            }
        };

        //Define q o runnable vai começar imediatamente sem dalay, e repitir após 10 segundos
        agendador.scheduleWithFixedDelay(tarefa, 0, INTERVALO_SEGUNDOS, TimeUnit.SECONDS);

        //Encerramento limpo, uma thread que roda qunando a JVM está encerrando
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //Sinaliza q o agendador nao tem mais nenhuma tarefa e o termina
            LOG.info("Encerrando o coletor...");
            agendador.shutdown();
            try {
                //Espera 15 segundos para o encerramento, caso não aconteça força shutdownNow
                if (!agendador.awaitTermination(15, TimeUnit.SECONDS)) {
                    agendador.shutdownNow();
                }
            //Se for interrompido tambem força a finalização e restaura o flag de interrupção
            } catch (InterruptedException e) {
                agendador.shutdownNow();
                Thread.currentThread().interrupt();
            }
            //fecha a fonte(libera http) e a SessionFactory do hibernate
            fonte.close();
            HibernateUtil.fechar();
        }));
    }
}