package com.Utc.RutaExpress.entity;

// Progreso de un envio; el backend solo debe permitir avanzar en este orden, nunca retroceder
public enum EstadoEnvio {
    REGISTRADO,
    EN_TRANSITO,
    EN_REPARTO,
    ENTREGADO
}
