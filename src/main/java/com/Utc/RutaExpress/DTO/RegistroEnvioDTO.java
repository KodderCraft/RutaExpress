package com.Utc.RutaExpress.DTO;

import java.math.BigDecimal;

public class RegistroEnvioDTO {
    private Long id; // Agregado para la actualización
    private String direccionRecogida;
    private String direccionEntrega;
    private String nombreRemitente;
    private String telefonoRemitente;
    private String nombreDestinatario;
    private String telefonoDestinatario;
    private String tipoServicio;
    private String pagador;
    private String instrucciones;
    private BigDecimal valorDeclarado;
    private Double distanciaKm;
    private Integer tiempoEstimadoMin;
    private BigDecimal costoTotal;
    private String descripcion;
    private Double peso;
    private Double alto;
    private Double ancho;
    private Double largo;
    private Boolean fragil;
    private String tipo;
    private Double latitudRecogida;
    private Double longitudRecogida;
    private Double latitudEntrega;
    private Double longitudEntrega;

    
    public String getDireccionRecogida() { return direccionRecogida; }
    public void setDireccionRecogida(String direccionRecogida) { this.direccionRecogida = direccionRecogida; }

    public String getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(String direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public String getNombreRemitente() { return nombreRemitente; }
    public void setNombreRemitente(String nombreRemitente) { this.nombreRemitente = nombreRemitente; }

    public String getTelefonoRemitente() { return telefonoRemitente; }
    public void setTelefonoRemitente(String telefonoRemitente) { this.telefonoRemitente = telefonoRemitente; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    public String getTelefonoDestinatario() { return telefonoDestinatario; }
    public void setTelefonoDestinatario(String telefonoDestinatario) { this.telefonoDestinatario = telefonoDestinatario; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public String getPagador() { return pagador; }
    public void setPagador(String pagador) { this.pagador = pagador; }

    public String getInstrucciones() { return instrucciones; }
    public void setInstrucciones(String instrucciones) { this.instrucciones = instrucciones; }

    public BigDecimal getValorDeclarado() { return valorDeclarado; }
    public void setValorDeclarado(BigDecimal valorDeclarado) { this.valorDeclarado = valorDeclarado; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

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

    public Double getLatitudRecogida() { return latitudRecogida; }
    public void setLatitudRecogida(Double latitudRecogida) { this.latitudRecogida = latitudRecogida; }

    public Double getLongitudRecogida() { return longitudRecogida; }
    public void setLongitudRecogida(Double longitudRecogida) { this.longitudRecogida = longitudRecogida; }

    public Double getLatitudEntrega() { return latitudEntrega; }
    public void setLatitudEntrega(Double latitudEntrega) { this.latitudEntrega = latitudEntrega; }

    public Double getLongitudEntrega() { return longitudEntrega; }
    public void setLongitudEntrega(Double longitudEntrega) { this.longitudEntrega = longitudEntrega; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
