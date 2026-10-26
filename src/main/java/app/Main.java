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


import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Localizacao> cidades = List.of(
                new Localizacao("São Paulo", -23.55, -46.63, "America/Sao_Paulo"),
                new Localizacao("Rio de Janeiro", -22.91, -43.17, "America/Sao_Paulo"),
                new Localizacao("Brasília", -15.79, -47.88, "America/Sao_Paulo"),
                new Localizacao("Salvador", -12.97, -38.50, "America/Sao_Paulo"),
                new Localizacao("Porto Alegre", -30.03, -51.23, "America/Sao_Paulo"));

        LocalizacaoDao localizacaoDao = new LocalizacaoDaoHibernate(HibernateUtil.getSessionFactory());
        PrevisaoDao previsaoDao = new PrevisaoDaoHibernate(HibernateUtil.getSessionFactory());
        ApresentadorClima presenter = new ApresentadorClima();

        try (ClienteClima client = new ClienteClima()) {
            ColetaService coleta = new ColetaService(client, localizacaoDao, previsaoDao);

            for (PrevisaoDoCLima p : coleta.coletar(cidades)) {
                presenter.exibir(p);
            }
        } finally {
            HibernateUtil.fechar();
        }
    }
}