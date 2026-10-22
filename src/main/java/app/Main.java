package app;

import apresentacao.ApresentadorClima;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;
import servico.ClienteClima;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // coordenadas aproximadas
        List<Localizacao> cidades = List.of(
                new Localizacao("São Paulo", -23.55, -46.63, "America/Sao_Paulo"),
                new Localizacao("Rio de Janeiro", -22.91, -43.17, "America/Sao_Paulo"),
                new Localizacao("Brasília", -15.79, -47.88, "America/Sao_Paulo"),
                new Localizacao("Salvador", -12.97, -38.50, "America/Sao_Paulo"),
                new Localizacao("Porto Alegre", -30.03, -51.23, "America/Sao_Paulo"));

        ClienteClima client = new ClienteClima();
        ApresentadorClima presenter = new ApresentadorClima();

        try(ClienteClima cliente = new ClienteClima()) {
            for (Localizacao cidade : cidades) {
                try {
                    PrevisaoDoCLima previsao = client.buscar(cidade);
                    presenter.exibir(previsao);
                } catch (Exception e) {
                    System.out.println("Falha em " + cidade.city() + ": " + e.getMessage());
                }
            }
        }
    }
}