package modelo;

import jakarta.persistence.*;

// nova entidade q determina a classe e define como tabela
@Entity
@Table(name = "locations")
public class Localizacao {

    // chave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // column serve para correlacionar o campo do java com a coluna do db
    @Column(nullable = false, length = 150)
    private String city;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false, length = 100)
    private String timezone;

    protected Localizacao() {}   // exigido pelo Hibernate

    public Localizacao(String city, double latitude, double longitude, String timezone) {
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timezone = timezone;
    }

    public Long getId() { return id; }
    public String getCity() { return city; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getTimezone() { return timezone; }
}