# Cambios en la rama `repartidor-dashboard`

Resumen de todo lo que se construyó en esta rama, agrupado por tema (no en orden
estrictamente cronológico de commits, aunque se referencia el commit de cada uno).
Para el "cómo funciona todo junto" ver [`README.md`](README.md).

## 1. Dashboard base: ver y elegir envíos

- `f26f8e1` — Permite al repartidor elegir envíos disponibles, con una meta diaria
  configurable (`app.repartidor.meta-diaria`).
- `b367b46` — Muestra en "Ruta de hoy" los envíos reales asignados al repartidor y
  cuántos ya completó.
- `405d235` — Fusiona `EnvioService` y su implementación en una sola clase (limpieza).
- `0456f6e` — Detecta envíos vencidos (`fechaLimite` pasada) y permite avanzar el
  estado paso a paso (Pendiente → Recogido → En camino → Entregado).
- `c592c80` — Reemplaza el panel de "entrega fija" por gestión individual por envío
  (`?gestionar={id}`), con su propio panel de detalle.

## 2. Entrega fallida, reintentos y devolución

- `bbb549a` — Primera versión de "entrega fallida": marcar un envío como no entregado
  y completar los detalles del servicio en el panel de gestión.
- `5fa2b8b` — Agrega el cálculo de "Ganado hoy" y permite eliminar del historial los
  envíos ya entregados.
- **`e555b22`** — Cambio grande: agrega el estado `DEVUELTO` y el contador
  `intentosEntrega`. Reescribe `marcarNoEntregado` para que, según el número de
  intentos, el envío vuelva a `PENDIENTE` (reintentable) o pase a `DEVUELTO`
  (terminal). Excluye `DEVUELTO` de "Ganado hoy" y del scheduler de vencidos.
- `7fb15e5` — Muestra el contador de intentos y el estado `DEVUELTO` en el dashboard.
- **`c704f91`** — Se agrega el estado intermedio `NO_ENTREGADO`: en vez de que un
  intento fallido vuelva en silencio a `PENDIENTE` (desapareciendo de la vista del
  repartidor), ahora se queda asignado y visible, con un botón **"Reintentar"** que lo
  regresa a `EN_CAMINO`. `CANCELADO` queda reservado solo para cancelaciones reales.
- **`7c0e21a`** — Permite eliminar también los envíos `DEVUELTO`: a diferencia de
  `ENTREGADO` (que se borra), un `DEVUELTO` se **libera** — vuelve a `PENDIENTE` sin
  repartidor, con intentos reiniciados, y reaparece en "Disponibles". `calcularGanadoHoy`
  deja de excluir `DEVUELTO`, así que esas ganancias siguen contando hasta que el
  repartidor elimina el envío explícitamente. También corrige un bug real: eliminar un
  `ENTREGADO` que tuvo una incidencia asociada fallaba con una violación de llave
  foránea (`incidencias.envio_id`) — ahora se borra la incidencia primero.

## 3. Ganancias con datos reales

- El panel "Ganancias" era 100% mockup (barras con alturas fijas, `$96.30` fijo,
  "Calificación promedio" y "Próximo pago" inventados). Se reemplazó por:
  - `EnvioRepository`: `findByRepartidorAndEstadoAndFechaEntregaBetween` y
    `findTop10ByRepartidorAndEstadoOrderByFechaEntregaDesc`.
  - `EnvioService`: `listarEntregadosSemana`, `calcularGanadoSemana`,
    `listarEntregasRecientes`.
  - `RepartidorController`: agrupa por día de la semana para las 7 barras reales.
  - Se sacaron "Calificación promedio" y "Próximo pago" — no hay sistema de
    calificaciones ni `Pago` real en el proyecto, así que no había forma honesta de
    mostrarlos.

## 4. Interfaz: modal de detalle, mapa de ruta, modal de confirmación

- **`94e0b3a`** —
  - Rediseña el modal de "Detalle de envío": avatar con iniciales del cliente, línea de
    ruta con puntos verde/rojo, cajas separadas para Tipo/Costo/Distancia/Tiempo/F.
    límite (antes era texto plano).
  - Agrega el botón "Detalle" a la tabla de "Disponibles" (verlo antes de elegirlo) y lo
    quita de "Ruta de hoy" (era redundante con "Gestionar").
  - Agrega un **mapa real** (Leaflet + Nominatim + OSRM) a la tarjeta "Ruta" del panel
    de Gestión — geocodifica las direcciones de texto en el navegador y dibuja la ruta,
    restringido a Ecuador (`countrycodes=ec`) para evitar resolver "Cuenca" a España.
  - *(Sin commit todavía)* — Ese mismo mapa se extendió también al modal de "Detalle"
    de Disponibles, y se pausó/reactivó la llamada automática mientras se seguía
    trabajando en otras funcionalidades.
- **`861fa7e`** — El pie del sidebar tenía "Jorge Torres" fijo en el HTML, sin relación
  con la sesión real. Ahora usa el usuario logueado (nombre, inicial del avatar) y el
  repartidor real (`vehiculoTipo`, `zona`). De paso corrige que `.sidebar-foot` tenía
  `position:fixed` sin ancho definido, lo que hacía que el texto se saliera del
  recuadro de 236px del sidebar.
- *(Sin commit todavía)* — Modal de confirmación propio (`#confirmModal`) para
  reemplazar los `confirm()`/`alert()` nativos del navegador en los 3 flujos de
  "Eliminar" y en la validación de motivo al marcar una entrega como no realizada.

## 5. Trabajo de infraestructura / equipo

- `a62c28a` — Merge de `master` (panel de administrador, interfaz de cliente) dentro de
  esta rama, sin conflictos.
- `e8dff4a` — Informe de errores encontrados al fusionar `master` (mismatch de
  contraseña de base de datos entre el equipo, check constraint desactualizado que
  bloqueaba el estado `DEVUELTO`, plantillas de administrador faltantes). Ver
  `informes/2026-07-18-merge-repartidor-dashboard/`.
- `c1a8114` — Restaura el login por nombre según `memory/repo-original` (el login por
  correo era una modificación local solo para pruebas, documentada en
  `memory/README.md`, que no debe quedar en el repositorio compartido).

## Datos de prueba

En algún punto se sembraron y luego se borraron varios lotes de envíos de prueba para
validar cada funcionalidad manualmente (reintentos, devolución, liberar a disponibles,
ganancias por semana, mapa de ruta). La base se dejó con **10 envíos limpios en estado
`PENDIENTE` sin repartidor** (códigos `SE-90101` a `SE-90110`), listos para que cualquiera
del equipo pruebe el flujo completo desde cero reclamándolos con las cuentas de
repartidor existentes ("Test" y "Repartidor Dos").
