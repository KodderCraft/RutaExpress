# Módulo de Repartidor — cómo funciona

Documentación técnica del dashboard de repartidor (`/repartidor/dashboard`), para que
cualquiera del equipo entienda el flujo completo sin tener que leer todo el código de
cero. Para el detalle de qué se hizo commit por commit, ver [`CAMBIOS.md`](CAMBIOS.md).

## Archivos clave

| Archivo | Qué hace |
|---|---|
| `entity/EstadoEnvio.java` | Enum de estados de un envío |
| `entity/Envio.java` | Entidad principal, incluye `intentosEntrega` e `isVencido()` |
| `service/EnvioService.java` | Toda la lógica de negocio de envíos (reclamar, avanzar estado, reintentos, ganancias) |
| `repository/EnvioRepository.java` | Queries derivadas de Spring Data |
| `controller/RepartidorController.java` | Único controller del módulo, arma el modelo para la vista |
| `templates/repartidor/dashboard.html` | Vista única con 5 paneles (tabs por JS, todos renderizados en el mismo HTML) |
| `static/scripts/controlDepaneles.js` | JS compartido con el dashboard de Cliente: navegación de paneles, modal de detalle, modal de confirmación, mapa de ruta |
| `static/css/stilos.css` | Estilos compartidos con Cliente/Admin |
| `service/IncidenciaService.java` | Registra incidencias (entregas fallidas/vencidas) para revisión de admin |
| `scheduler/EnvioVencimientoScheduler.java` | Job cada 5 min que marca incidencias para envíos vencidos no resueltos |

## El estado de un envío

```
PENDIENTE ──(reclamar)──> RECOGIDO ──(avanzar)──> EN_CAMINO ──(entregar)──> ENTREGADO
                                                        │
                                                        ├──(no se pudo entregar,
                                                        │   intentos < máximo)──> NO_ENTREGADO
                                                        │                              │
                                                        │                        (reintentar)
                                                        │                              │
                                                        │                              ▼
                                                        │                         EN_CAMINO
                                                        │
                                                        └──(no se pudo entregar,
                                                            intentos = máximo)───> DEVUELTO
```

- **`CANCELADO`** es un estado aparte, reservado para cancelaciones reales (no se usa en
  el flujo de entrega fallida).
- El número máximo de intentos es configurable:
  `app.repartidor.max-intentos-entrega` en `application.properties` (actualmente `3`).
- `Envio.intentosEntrega` cuenta los intentos fallidos acumulados. Al fallar:
  - Si `intentos < máximo` → pasa a `NO_ENTREGADO`, **se queda asignado al mismo
    repartidor** (no vuelve a Disponibles solo). Aparece con badge y botón
    "Reintentar" que lo regresa a `EN_CAMINO`.
  - Si `intentos = máximo` → pasa a `DEVUELTO` (terminal). El repartidor puede
    "Eliminar" ese envío: en vez de borrarlo, **lo libera** — vuelve a `PENDIENTE`,
    sin repartidor, con intentos reiniciados a 0, y reaparece en "Disponibles" para que
    otro repartidor lo intente desde cero.
  - `ENTREGADO` sí se puede "Eliminar" de verdad (borra la fila) — ahí se limpia
    también su `Incidencia` asociada si tenía una, porque hay una FK que lo impide si no.

## Los 5 paneles del dashboard

### 1. Ruta de hoy (`data-panel="inicio"`)
Lista los envíos asignados al repartidor **hoy** (`fechaAsignacion` dentro del día
actual — `EnvioService.listarAsignadosHoy`). Desde acá se gestiona cada envío
("Gestionar" → panel de Gestión) y se reintentan/eliminan según su estado.

### 2. Disponibles (`data-panel="disponibles"`)
Envíos en `PENDIENTE` sin repartidor asignado (`listarDisponibles`). Cada fila tiene un
botón **"Detalle"** (abre un modal con contacto, mapa de ruta y datos del servicio,
antes de decidir) y un botón **"Elegir"** que llama a `reclamar()` — una consulta
`UPDATE ... WHERE repartidor IS NULL AND estado = PENDIENTE` atómica, para evitar que
dos repartidores tomen el mismo envío a la vez.

### 3. Mapa (`data-panel="mapa"`)
**Sigue siendo un SVG estático dibujado a mano** — no es un mapa real, es solo
decorativo. No se tocó en esta rama.

### 4. Gestión de envío (panel `entrega`, se activa con `?gestionar={id}`)
El detalle completo de un envío puntual: progreso (stepper), contacto, ruta (con mapa
real, ver abajo), detalles del servicio, y las acciones según el estado actual
(avanzar, marcar entregado, marcar no entregado, reintentar, eliminar).

### 5. Ganancias (`data-panel="ganancias"`)
Todo con datos reales, calculado sobre envíos `ENTREGADO`:
- **Ganado hoy**: reutiliza `calcularGanadoHoy` (suma de todo lo asignado hoy que no
  esté `CANCELADO`, incluye trabajo en curso).
- **Ganado esta semana** / **Entregas completadas esta semana**: solo cuenta envíos que
  llegaron a `ENTREGADO`, usando su `fechaEntrega`, dentro de la semana actual
  (lunes-domingo).
- **Gráfica de 7 barras**: monto ganado por día, calculado agrupando por
  `fechaEntrega.getDayOfWeek()`. La barra de hoy se resalta con otro color.
- **Historial de entregas**: últimas 10 entregas completadas (código, fecha, costo).
- Se sacaron del mockup original "Calificación promedio" y "Próximo pago" porque no
  hay ningún dato real detrás (no existe sistema de calificaciones; `Pago` está
  huérfano en el proyecto, nunca se llena).

## El mapa de ruta

Tanto en "Gestión de envío" como en el modal de "Detalle" (Disponibles) hay un mapa real
con la ruta entre la dirección de recogida y la de entrega. Cómo funciona:

1. Al abrir el panel/modal, JS toma el **texto** de ambas direcciones
   (`Envio.direccionRecogida.direccionTexto` / `direccionEntrega.direccionTexto`).
2. Geocodifica cada texto con **Nominatim** (OpenStreetMap, gratis, sin API key),
   restringido a Ecuador (`countrycodes=ec`) — sin esto, direcciones como "Cuenca" se
   pueden resolver a la ciudad homónima en España.
3. Dibuja el mapa con **Leaflet** y pide la ruta entre los dos puntos a **OSRM**
   (routing público, también gratis), mostrando distancia y tiempo.

**Limitación importante**: el proyecto nunca guarda coordenadas (`latitud`/`longitud`)
de ninguna dirección — ni siquiera el selector de mapa del cliente al crear un envío
las persiste (no existe `DireccionRepository`). Por eso esto geocodifica el texto **en
vivo, cada vez que se abre la pantalla**, en el navegador. La función reutilizable es
`dibujarMapaRuta(contenedorId, infoElId, recogidaTexto, entregaTexto)` en
`controlDepaneles.js`.

## Modales (reemplazo de `alert()`/`confirm()`)

Todo el módulo usa dos modales propios en vez de los diálogos nativos del navegador:

- **`#envioDetalleOverlay`**: detalle de un envío (usado desde "Disponibles"), incluye
  el mapa de ruta.
- **`#confirmModal`**: modal genérico de confirmación/alerta, con dos modos:
  - `confirmarAccion(form, mensaje)` — botones Cancelar/Confirmar; al confirmar hace
    `form.submit()`. Se usa en los 3 flujos de "Eliminar".
  - `mostrarAlertaModal(mensaje)` — un solo botón "Entendido". Se usa cuando falta
    seleccionar un motivo antes de marcar una entrega como no realizada.

## Configuración (`application.properties`)

```properties
app.repartidor.meta-diaria=5           # meta de envíos a elegir por día, solo visual
app.repartidor.plazo-entrega-horas=24  # horas desde que se reclama hasta la fecha límite
app.repartidor.max-intentos-entrega=3  # intentos antes de pasar a DEVUELTO
```

## Modelo de cobro

El proyecto no tiene pago prepagado real — todo el dinero se modela como **contra
entrega (COD)**: nunca se cobra nada hasta que el envío llega a `ENTREGADO`. Por eso:
- Un envío `NO_ENTREGADO` o en curso sigue contando en "Ganado hoy" (proyección de lo
  que se espera cobrar), pero un `DEVUELTO` **no cuenta como ganancia real** en el panel
  de Ganancias, salvo mientras el repartidor no lo elimine (en "Ganado hoy" de la ruta
  del día sí sigue contando hasta que se libera).
- No se toca `Pago`/`EstadoPago`/`PagoRepository` en ningún punto de este módulo — están
  huérfanos en el proyecto entero (ver `informes/2026-07-18-merge-repartidor-dashboard/`).

## Cosas que quedan pendientes / fuera de alcance

- El panel "Mapa" (SVG estático) no se convirtió a mapa real.
- Las plantillas de administrador (`administrador/pagos.html`, `envios.html`,
  `incidencias.html`) no existen — sus rutas en `AdminController` rompen con 500.
- No hay forma de que un admin reasigne manualmente un envío `DEVUELTO` a un repartidor
  específico — solo vuelve al pool general de "Disponibles".
- El mapa de ruta depende de servicios públicos externos (Nominatim/OSRM) sin caché ni
  manejo de límite de peticiones — si el proyecto crece, conviene revisar esto.
