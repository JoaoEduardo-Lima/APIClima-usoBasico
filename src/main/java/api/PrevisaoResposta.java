package api;

// Resposta completa da API, contendo as infos de local junto com os dados metereologicos
//recebe as infos em forma de bloco JSON e os transforma em objetos java

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrevisaoResposta {
    public double latitude;
    public double longitude;
    public String timezone;
    public CondicaoAtual current;
}