package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CondicaoAtual {
    public String time;

    @JsonProperty("temperature_2m")
    public double temperatura;

    @JsonProperty("apparent_temperature")
    public double sensacaoTermica;

    @JsonProperty("relative_humidity_2m")
    public int umidade;

    @JsonProperty("precipitation")
    public double precipitacao;

    @JsonProperty("weather_code")
    public int codigoTempo;

    @JsonProperty("wind_speed_10m")
    public double vento;

    @JsonProperty("wind_direction_10m")
    public double direcaoVento;

    @JsonProperty("pressure_msl")
    public double pressao;

    @JsonProperty("cloud_cover")
    public double nuvens;
}