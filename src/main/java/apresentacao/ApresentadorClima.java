package apresentacao;

import modelo.CondicaoClimatica;
import modelo.PrevisaoDoCLima;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ApresentadorClima {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void exibir(PrevisaoDoCLima f) {
        ZoneId zona = ZoneId.of(f.getLocation().getTimezone());
        String condicao = CondicaoClimatica.deCodigo(f.getWeatherCode()).getDescricao();

        System.out.println("=== " + f.getLocation().getCity() + " ===");
        System.out.println("Previsão para:    " + f.getForecastAt().atZone(zona).format(FORMATO));
        System.out.println("Condição:         " + condicao);
        System.out.printf("Temperatura:      %.1f °C%n", f.getTemperature());
        System.out.printf("Sensação térmica: %.1f °C%n", f.getApparentTemperature());
        System.out.println("Umidade:          " + f.getHumidity() + " %");
        System.out.printf("Precipitação:     %.1f mm%n", f.getPrecipitation());
        System.out.printf("Vento:            %.1f km/h (direção %.0f°)%n", f.getWindSpeed(), f.getWindDirection());
        System.out.printf("Pressão:          %.1f hPa%n", f.getPressure());
        System.out.printf("Nuvens:           %.0f %%%n", f.getCloudCover());
        System.out.println("Consultado em:    " + f.getUpdatedAt().atZone(zona).format(FORMATO));
        System.out.println();
    }
}