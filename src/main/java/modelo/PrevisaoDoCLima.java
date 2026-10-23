package modelo;

import jakarta.persistence.*;
import java.time.Instant;

//marca a classe como mapeada e dá o nome da tabela
@Entity
@Table(name = "weather_forecasts")
public class PrevisaoDoCLima {

    // chave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Localizacao location;

    // coloumn liga o campo do java a coluna do db
    @Column(name = "forecast_at", nullable = false)
    private Instant forecastAt;

    private double temperature;

    @Column(name = "apparent_temperature", nullable = false)
    private double apparentTemperature;

    private int humidity;
    private double precipitation;

    @Column(name = "weather_code", nullable = false)
    private int weatherCode;

    @Column(name = "wind_speed", nullable = false)
    private double windSpeed;

    @Column(name = "wind_direction", nullable = false)
    private double windDirection;

    private double pressure;

    @Column(name = "cloud_cover", nullable = false)
    private double cloudCover;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PrevisaoDoCLima() {}   // exigido pelo Hibernate

    // mesma ordem do antigo record, então o WeatherClient continua igual
    public PrevisaoDoCLima(Localizacao location, Instant forecastAt, double temperature,
                           double apparentTemperature, int humidity, double precipitation,
                           int weatherCode, double windSpeed, double windDirection,
                           double pressure, double cloudCover, Instant updatedAt) {
        this.location = location;
        this.forecastAt = forecastAt;
        this.temperature = temperature;
        this.apparentTemperature = apparentTemperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.weatherCode = weatherCode;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.pressure = pressure;
        this.cloudCover = cloudCover;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Localizacao getLocation() { return location; }
    public Instant getForecastAt() { return forecastAt; }
    public double getTemperature() { return temperature; }
    public double getApparentTemperature() { return apparentTemperature; }
    public int getHumidity() { return humidity; }
    public double getPrecipitation() { return precipitation; }
    public int getWeatherCode() { return weatherCode; }
    public double getWindSpeed() { return windSpeed; }
    public double getWindDirection() { return windDirection; }
    public double getPressure() { return pressure; }
    public double getCloudCover() { return cloudCover; }
    public Instant getUpdatedAt() { return updatedAt; }
}