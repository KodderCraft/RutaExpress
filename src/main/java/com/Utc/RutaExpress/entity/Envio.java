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

    // Quién paga el envío: "REMITENTE" (prepagado por quien envía) o "DESTINATARIO"
    // (pago contra entrega, lo cobra el repartidor al entregar).
    private String pagador = "REMITENTE";

    private Double distanciaKm;

    private Integer tiempoEstimadoMin;

    private LocalDateTime fechaRegistro;

    // Cuándo el repartidor marcó el envío como RECOGIDO. Se usa para las ganancias cuando
    // paga el remitente (prepagado): la ganancia se cuenta en este momento, no en la entrega.
    private LocalDateTime fechaRecogido;

    private LocalDateTime fechaEntrega;

    private LocalDateTime fechaAsignacion;

    private LocalDateTime fechaLimite;

    // Cuenta los intentos de entrega fallidos. Al llegar a app.repartidor.max-intentos-entrega
    // el envio pasa a DEVUELTO en vez de volver a NO_ENTREGADO (ver EnvioService.marcarNoEntregado).
    private Integer intentosEntrega = 0;

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

    public String getPagador() { return pagador; }
    public void setPagador(String pagador) { this.pagador = pagador; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public Integer getTiempoEstimadoMin() { return tiempoEstimadoMin; }
    public void setTiempoEstimadoMin(Integer tiempoEstimadoMin) { this.tiempoEstimadoMin = tiempoEstimadoMin; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaRecogido() { return fechaRecogido; }
    public void setFechaRecogido(LocalDateTime fechaRecogido) { this.fechaRecogido = fechaRecogido; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(LocalDateTime fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public LocalDateTime getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDateTime fechaLimite) { this.fechaLimite = fechaLimite; }

    public Integer getIntentosEntrega() { return intentosEntrega; }
    public void setIntentosEntrega(Integer intentosEntrega) { this.intentosEntrega = intentosEntrega; }

    @Transient
    public boolean isVencido() {
        return fechaLimite != null
                && estado != EstadoEnvio.ENTREGADO && estado != EstadoEnvio.CANCELADO
                && estado != EstadoEnvio.DEVUELTO
                && fechaLimite.isBefore(LocalDateTime.now());
    }
}
