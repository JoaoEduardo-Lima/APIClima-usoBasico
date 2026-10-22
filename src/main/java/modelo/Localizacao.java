package modelo;
// modelo responsavel por determinar a localização de cada cidade
public record Localizacao(
        String city,
        double latitude,
        double longitude,
        String timezone) {}