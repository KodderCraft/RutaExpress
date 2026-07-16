package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "paquetes")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id")
    private Envio envio;

    private String descripcion;

    private Double peso;

    private Double alto;

    private Double ancho;

    private Double largo;

    private Boolean fragil;

    private String tipo;

    private BigDecimal valorDeclarado;

    public Paquete() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Envio getEnvio() { return envio; }
    public void setEnvio(Envio envio) { this.envio = envio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAlto() { return alto; }
    public void setAlto(Double alto) { this.alto = alto; }

    public Double getAncho() { return ancho; }
    public void setAncho(Double ancho) { this.ancho = ancho; }

    public Double getLargo() { return largo; }
    public void setLargo(Double largo) { this.largo = largo; }

    public Boolean getFragil() { return fragil; }
    public void setFragil(Boolean fragil) { this.fragil = fragil; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getValorDeclarado() { return valorDeclarado; }
    public void setValorDeclarado(BigDecimal valorDeclarado) { this.valorDeclarado = valorDeclarado; }
}
