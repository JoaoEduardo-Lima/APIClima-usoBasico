package apresentacao;

import modelo.CondicaoClimatica;
import modelo.PrevisaoDoCLima;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ApresentadorClima {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void exibir(PrevisaoDoCLima f) {
        ZoneId zona = ZoneId.of(f.location().timezone());
        String condicao = CondicaoClimatica.deCodigo(f.weatherCode()).getDescricao();

        System.out.println("=== " + f.location().city() + " ===");
        System.out.println("Previsão para:    " + f.forecastAt().atZone(zona).format(FORMATO));
        System.out.println("Condição:         " + condicao);
        System.out.printf("Temperatura:      %.1f °C%n", f.temperature());
        System.out.printf("Sensação térmica: %.1f °C%n", f.apparentTemperature());
        System.out.println("Umidade:          " + f.humidity() + " %");
        System.out.printf("Precipitação:     %.1f mm%n", f.precipitation());
        System.out.printf("Vento:            %.1f km/h (direção %.0f°)%n", f.windSpeed(), f.windDirection());
        System.out.printf("Pressão:          %.1f hPa%n", f.pressure());
        System.out.printf("Nuvens:           %.0f %%%n", f.cloudCover());
        System.out.println("Consultado em:    " + f.updatedAt().atZone(zona).format(FORMATO));
        System.out.println();
    }
}