package servico;

import api.CondicaoAtual;
import api.PrevisaoResposta;
import com.fasterxml.jackson.databind.ObjectMapper;
import modelo.Localizacao;
import modelo.PrevisaoDoCLima;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ClienteClima implements AutoCloseable {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public PrevisaoDoCLima buscar(Localizacao local) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(montarUri(local))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Erro na API (status " + response.statusCode() + "): " + response.body());
        }

        // JSON -> formato da API
        PrevisaoResposta resposta = mapper.readValue(response.body(), PrevisaoResposta.class);
        CondicaoAtual atual = resposta.current;

        //
        Instant momentoDaPrevisao = LocalDateTime.parse(atual.time)
                .atZone(ZoneId.of(local.getTimezone()))
                .toInstant();

        // adequa o formato da api pro formato criado
        return new PrevisaoDoCLima(
                local,
                momentoDaPrevisao,
                atual.temperatura,
                atual.sensacaoTermica,
                atual.umidade,
                atual.precipitacao,
                atual.codigoTempo,
                atual.vento,
                atual.direcaoVento,
                atual.pressao,
                atual.nuvens,
                Instant.now());
        }


                //monta a URL a partir de um mapa de parâmetros
    private URI montarUri(Localizacao local) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("latitude", String.valueOf(local.getLatitude()));
        params.put("longitude", String.valueOf(local.getLongitude()));
        params.put("current", String.join(",",
                "temperature_2m",
                "apparent_temperature",
                "relative_humidity_2m",
                "precipitation",
                "weather_code",
                "wind_speed_10m",
                "wind_direction_10m",
                "pressure_msl",
                "cloud_cover"));
        params.put("timezone", local.getTimezone());

        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return URI.create(BASE_URL + "?" + query);
    }


    @Override
    public void close() {
        client.close();
    }
}