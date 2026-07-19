package com.Utc.RutaExpress.entity;

public enum EstadoEnvio {
    PENDIENTE,
    RECOGIDO,
    EN_CAMINO,
    ENTREGADO,
    // Cancelacion real (admin/cliente). No se usa para una entrega fallida.
    CANCELADO,
    // Intento de entrega fallido pero aun quedan reintentos (ver EnvioService.marcarNoEntregado).
    // Se queda asignado al mismo repartidor y puede volver a EN_CAMINO con reintentarEntrega.
    NO_ENTREGADO,
    // Se agotaron los intentos (app.repartidor.max-intentos-entrega). Terminal: solo se
    // puede "eliminar" (EnvioService.eliminarEntregado), lo que en realidad lo libera de
    // vuelta a PENDIENTE sin repartidor para que otro lo intente.
    DEVUELTO
}
