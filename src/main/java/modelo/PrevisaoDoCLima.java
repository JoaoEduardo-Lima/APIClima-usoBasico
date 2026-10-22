package modelo;

import java.time.Instant;

public record PrevisaoDoCLima(
        Localizacao location,
        Instant forecastAt,
        double temperature,
        double apparentTemperature,
        int humidity,
        double precipitation,
        int weatherCode,
        double windSpeed,
        double windDirection,
        double pressure,
        double cloudCover,
        Instant updatedAt) {}