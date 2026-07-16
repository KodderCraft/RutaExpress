package com.Utc.RutaExpress.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "envios")
public class Envio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoGuia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remitente_id")
    private Usuario remitente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id")
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Repartidor repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_recogida_id")
    private Direccion direccionRecogida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direccion_entrega_id")
    private Direccion direccionEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    private String tipoServicio;

    @Enumerated(EnumType.STRING)
    private EstadoEnvio estado;

    private BigDecimal costoTotal;

    private Double distanciaKm;

    private Integer tiempoEstimadoMin;

    private LocalDateTime fechaRegistro;

    private LocalDateTime fechaEntrega;

    private LocalDateTime fechaAsignacion;

    public Envio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoGuia() { return codigoGuia; }
    public void setCodigoGuia(String codigoGuia) { this.codigoGuia = codigoGuia; }

    public Usuario getRemitente() { return remitente; }
    public void setRemitente(Usuario remitente) { this.remitente = remitente; }

    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

    public Repartidor getRepartidor() { return repartidor; }
    public void setRepartidor(Repartidor repartidor) { this.repartidor = repartidor; }

    public Direccion getDireccionRecogida() { return direccionRecogida; }
    public void setDireccionRecogida(Direccion direccionRecogida) { this.direccionRecogida = direccionRecogida; }

    public Direccion getDireccionEntrega() { return direccionEntrega; }
    public void setDireccionEntrega(Direccion direccionEntrega) { this.direccionEntrega = direccionEntrega; }

    public Tarifa getTarifa() { return tarifa; }
    public void setTarifa(Tarifa tarifa) { this.tarifa = tarifa; }

    public String getTipoServicio() { return tipoServicio; }
    public void setTipoServicio(String tipoServicio) { this.tipoServicio = tipoServicio; }

    public EstadoEnvio getEstado() { return estado; }
    public void setEstado(EstadoEnvio estado) { this.estado = estado; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }
}
