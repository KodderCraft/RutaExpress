# Registro de envíos, tarifas por distancia, pagador y ganancias — cómo funciona

Documentación de todo lo hecho en la rama `feature/tarifas-pagador-ganancias`. Esta rama
parte de `repartidor-dashboard` y completa el merge de `funcionalidades` (registro y
edición de envíos del lado del cliente), que hasta ese momento estaba a medias y con un
bug que hacía que **ningún envío se guardara de verdad**. Sobre esa base se construyó todo
lo demás: costo por distancia, quién paga el envío, y ganancias del repartidor.

## Resumen en una frase por funcionalidad

1. El registro de envíos del cliente ahora sí guarda en la base de datos (antes la
   llamada de guardado estaba comentada).
2. El costo de un envío se calcula en el **backend**, como `precio_por_km(tipo de
   servicio) × distancia real de la ruta`, no un precio fijo ni un valor que manda el
   navegador.
3. El cliente elige, al crear el envío, **quién paga**: él (remitente) o el destinatario
   (contra entrega).
4. Las **ganancias del repartidor** dependen de esa elección: si paga el remitente, se
   cuentan al marcar el envío como *recogido*; si paga el destinatario, se cuentan al
   marcarlo como *entregado*.
5. El cliente ahora tiene una pestaña **"Recibidos"** para ver los envíos donde es
   destinatario (antes no existía ninguna vista para eso).
6. Se corrigieron varios bugs de UI/JS que impedían que el precio se calculara o se
   mostrara correctamente, y uno de backend que hacía fallar el borrado de envíos
   entregados.

## Archivos nuevos

| Archivo | Qué es |
|---|---|
| `DTO/RegistroEnvioDTO.java` | Datos del formulario de "Nuevo envío" / "Editar envío" (direcciones, destinatario, tipo de servicio, pagador, datos del paquete, costo calculado) |
| `controller/EnvioController.java` | Maneja el registro, edición y eliminación de envíos desde el panel del cliente |
| `service/EnvioServiceImpl.java` | Implementación de `EnvioService`: toda la lógica de negocio de envíos (cliente **y** repartidor, unificados en una sola interfaz) |
| `repository/DireccionRepository.java` | CRUD de `Direccion` (recogida/entrega) |
| `repository/PaqueteRepository.java` | CRUD de `Paquete` (datos físicos del paquete: peso, dimensiones, frágil, etc.) |
| `repository/TarifaRepository.java` | CRUD de `Tarifa` (precio por km por tipo de servicio) |
| `templates/Cliente/editar-envio.html` | Formulario para editar un envío ya creado (solo si todavía no tiene repartidor asignado) |
| `static/scripts/mapa.js` | Todo el JS del mapa de selección de direcciones y cálculo de ruta/costo, para el lado del cliente |
| `test/.../EnvioControllerTest.java` | Pruebas de `EnvioController` (quedaron comentadas por el equipo, no se reactivaron) |

## Archivos modificados y qué cambió en cada uno

| Archivo | Qué cambió |
|---|---|
| `pom.xml` | Corrige un typo real: la dependencia de test se llamaba `spring-boot-starter-webmvc-test` (no existe) en vez de `spring-boot-starter-test` |
| `entity/Envio.java` | Nuevos campos: `pagador` (`REMITENTE`/`DESTINATARIO`, default `REMITENTE`) y `fechaRecogido` (cuándo se marcó como recogido, usado para las ganancias) |
| `repository/EnvioRepository.java` | Nuevas queries: `findByRemitenteId`, `findByDestinatarioId` (del merge), y `findByRepartidorAndPagadorAndFechaRecogidoBetween` / `findByRepartidorAndPagadorAndEstadoAndFechaEntregaBetween` (para las ganancias por pagador) |
| `repository/UsuarioRepository.java` | Nueva query `findByNombreAndTelefono`, usada para no duplicar destinatarios que ya existen como usuario |
| `service/EnvioService.java` | Pasó de ser una clase concreta a una **interfaz** que junta los métodos de repartidor (reclamar, avanzar estado, ganancias...) con los de cliente (registrar, editar, listar por remitente/destinatario...) — ver sección "Cómo quedó la arquitectura" |
| `controller/RepartidorController.java` | Usa `listarGananciasSemana` (en vez de solo entregas) para las barras de "Ganado por día"; agrega la columna Remitente en "Disponibles" |
| `controller/controllerPrincipal.java` | El dashboard del cliente ahora expone `envios`, `enviosRecibidos` y `tarifasPorKm` al modelo (antes solo mostraba el nombre de usuario) |
| `application.properties` | La contraseña de Postgres quedó en `1231` (la que usa el `docker-compose.yml` del equipo) en vez de `postgres` |
| `static/css/stilos.css` | Ajustes menores de espaciado, sin cambios funcionales |
| `static/scripts/controlDepaneles.js` | Ajustes del merge para convivir con los paneles nuevos del cliente |
| `templates/Cliente/dashboard.html` | Reescrito casi por completo: formulario de "Nuevo envío" real (antes era estático), tabla de "Envíos recientes", nueva pestaña "Recibidos", columna "Pago", campos visibles de costo, radio buttons de "quién paga", mapa de ruta (corregido de posición, ver más abajo) |
| `templates/componentes/sidebarCliente.html` | Ítem de navegación "Mis envíos" → **"Recibidos"** (funcionalidad distinta); "Pagos" → "Saldo" |
| `templates/repartidor/dashboard.html` | Columna "Remitente" en la tabla de "Disponibles" |

## Cómo quedó la arquitectura de `EnvioService`

Antes del merge existían **dos cosas separadas** con el mismo nombre:
- En `repartidor-dashboard`: una **clase** `EnvioService` con toda la lógica del repartidor
  (reclamar, avanzar estado, ganancias, etc.).
- En `funcionalidades`: una **interfaz** `EnvioService` (para inyección de dependencias)
  con los métodos del cliente, implementada por `EnvioServiceImpl`.

Se unificaron en una sola interfaz `EnvioService` que declara **todos** los métodos (los
de repartidor + los de cliente), y una sola implementación `EnvioServiceImpl` que los
tiene todos. Todos los controllers (`RepartidorController`, `EnvioController`,
`controllerPrincipal`, `EnvioVencimientoScheduler`) dependen de la interfaz, no de la
implementación concreta (excepto `EnvioController`, que quedó dependiendo directo de
`EnvioServiceImpl` porque así vino del merge — funciona igual, Spring solo tiene un bean
de ese tipo).

## El bug que arreglamos primero: el envío nunca se guardaba

En `EnvioController.registrarEnvio`, la línea que debía persistir el envío estaba
**comentada**:

```java
// envioServiceImpl.registrar(registroEnvioDTO);
```

El formulario validaba los campos, mostraba "Envío registrado con éxito" y redirigía —
pero nunca había un `INSERT`. Por eso el envío no aparecía ni en "Disponibles" del
repartidor, ni en el perfil del remitente, ni en el del destinatario: sencillamente no
existía. Se corrigió leyendo el usuario de la sesión y llamando al método real:

```java
envioServiceImpl.registrarEnvio(usuarioSesion, registroEnvioDTO);
```

## Costo del envío: tarifa por km × distancia

La distancia se calcula en el navegador (sin Google Maps): **Nominatim** (OpenStreetMap)
convierte el punto que clickeas en el mapa a una dirección legible, y **OSRM** calcula la
ruta real por calles entre recogida y entrega, devolviendo distancia y tiempo.

El costo, en cambio, **lo decide el backend**, no el navegador — así nadie puede
manipular el campo oculto `costoTotal` antes de enviarlo:

```java
// EnvioServiceImpl.calcularCostoPorDistancia
costoTotal = tarifa.getPrecioBase() (= precio por km del tipo de servicio) × distanciaKm
```

`Tarifa.precioBase` se reutiliza como "precio por km" (no como cargo fijo). Los valores
actuales en la tabla `tarifas`:

| Tipo de servicio | Precio por km |
|---|---|
| Estándar | $0.10 |
| Express | $0.25 |
| Prioritario | $0.50 |

Si algún tipo de servicio no existe todavía en la tabla, `EnvioServiceImpl` lo crea al
vuelo usando el mismo mapa de valores (`PRECIO_POR_KM_POR_TIPO`), así que el respaldo en
código y los datos en la BD siempre deberían coincidir. Para cambiar los precios: o se
actualiza la tabla `tarifas` directamente, o se cambia `PRECIO_POR_KM_POR_TIPO` en
`EnvioServiceImpl` (y los valores de respaldo en `mapa.js` / `dashboard.html`, que se usan
solo si por algún motivo `tarifasPorKm` no llega al navegador).

**Validación importante**: si no se calculó la ruta (`distanciaKm` vacío o cero), el
backend **rechaza** el registro del envío con el mensaje *"Debes calcular la ruta ('Ver
ruta') antes de generar el envío"* — no se puede crear un envío con costo $0 o inventado.

### Cálculo automático, sin pasos manuales

Antes había que: elegir recogida → elegir entrega → presionar "Ver ruta" a mano. Ahora,
en cuanto se elige la segunda dirección (recogida o entrega, la que falte),
`guardarDireccion()` en `mapa.js` llama automáticamente a `verRuta()`. El botón "Ver ruta"
sigue existiendo por si hace falta recalcular manualmente (por ejemplo si OSRM falla).

El precio visible en pantalla (`costoTotalVisible` en el formulario, `costoRuta` en el
panel "Resumen") se recalcula también si el usuario cambia el tipo de servicio después de
calcular la ruta, usando la distancia ya guardada — no hace falta volver a presionar "Ver
ruta". Esto lee `window.TARIFAS_POR_KM`, un objeto inyectado desde el backend
(`controllerPrincipal` → `envioService.obtenerTarifasPorKm()`) para que el precio que se
ve en pantalla sea siempre el mismo que va a cobrar el servidor.

## Quién paga el envío (`pagador`)

Al crear o editar un envío, el cliente elige con radio buttons:
- **"Yo (remitente)"** — el envío queda prepagado, valor por defecto.
- **"El destinatario (contra entrega)"** — se cobra al momento de la entrega.

Esto se guarda en el nuevo campo `Envio.pagador` (`"REMITENTE"` o `"DESTINATARIO"`). Se
muestra en ambas tablas del cliente:
- "Envíos recientes" (remitente): *"Tú pagas"* / *"Paga el destinatario"*.
- "Recibidos" (destinatario): *"Ya pagado por el remitente"* / *"Tú pagas (contra
  entrega)"*.

## Ganancias del repartidor según quién paga

Antes, "Ganado hoy" sumaba **todo** lo asignado en el día (menos lo `CANCELADO`), sin
importar si ya se había cobrado o no — era una proyección. Ahora depende de la regla de
negocio (`EnvioServiceImpl.seGano`):

```java
private boolean seGano(Envio envio) {
    if (envio.getEstado() == EstadoEnvio.CANCELADO) return false;
    if ("DESTINATARIO".equalsIgnoreCase(envio.getPagador())) {
        return envio.getEstado() == EstadoEnvio.ENTREGADO;   // se gana al entregar
    }
    return envio.getFechaRecogido() != null;                 // se gana al recoger
}
```

- **Paga el remitente** → se cuenta la ganancia en cuanto el repartidor presiona
  "Marcar como recogido" (se guarda `Envio.fechaRecogido` en ese momento, dentro de
  `avanzarEstado`).
- **Paga el destinatario** → se cuenta solo cuando el repartidor presiona "Marcar como
  entregado" (contra entrega — recién ahí hay dinero real de por medio).

Esto aplica a:
- **"Ganado hoy"** (`calcularGanadoHoy`): filtra los envíos asignados hoy con `seGano`.
- **"Ganado esta semana"** (`calcularGanadoSemana` / `listarGananciasSemana`): combina dos
  listas — envíos que paga el remitente con `fechaRecogido` dentro de la semana, y envíos
  que paga el destinatario con `estado=ENTREGADO` y `fechaEntrega` dentro de la semana.
- **Barras de "Ganado por día"**: agrupan por la fecha correspondiente según quién paga
  cada envío (`fechaRecogido` o `fechaEntrega`), para que la suma de las barras siempre
  coincida con "Ganado esta semana".

Lo que **no cambió**: "Entregas completadas esta semana" y el "Historial de entregas"
siguen contando solo envíos `ENTREGADO` — esas métricas son sobre trabajo completado, no
sobre dinero ganado, y un envío "pagado por remitente y ya recogido pero aún no entregado"
sigue en curso, no es todavía una entrega completada.

**Nota**: los envíos que ya existían en la base antes de este cambio tenían `pagador`
vacío; se actualizaron manualmente a `REMITENTE` (su comportamiento por defecto) para que
no quedaran fuera de este cálculo.

## Pestaña "Recibidos" del cliente

`controllerPrincipal.mostrarDashboardCliente` ya calculaba `enviosRecibidos`
(`listarPorDestinatario`) para contar un total, pero **nunca lo pasaba a la vista** — no
existía ninguna tabla ni pestaña para mostrarlo. Se agregó:
- El ítem "Recibidos" en el sidebar del cliente.
- Un panel nuevo con una tabla (código, remitente, fecha, estado, repartidor, costo,
  quién paga) que itera sobre `enviosRecibidos`.

## Columna "Remitente" en "Disponibles" (repartidor)

La tabla de envíos disponibles para reclamar solo mostraba el destinatario. El dato del
remitente ya estaba disponible (`envio.remitente`, usado en el modal de detalle), solo
faltaba la columna en la tabla.

## Bugs de UI/JS corregidos

### 1. Código suelto en `mapa.js` rompía el script en cada carga de página

Justo después de declarar las variables globales, había un bloque `if/else` **fuera de
cualquier función** que hacía referencia a una variable `pos` que no existía en ese
contexto (solo existe dentro de `guardarDireccion()`, más abajo en el archivo). Esto
lanzaba un `ReferenceError` en cada carga de página, lo que impedía que el resto del
script se ejecutara — en particular, el listener que recalcula el precio al cambiar el
tipo de servicio, y la validación del formulario antes de enviarlo, quedaban **sin
registrar nunca**. Era una copia duplicada por error (el bloque correcto ya existía
dentro de `guardarDireccion()`); se eliminó la copia suelta.

### 2. El mapa de ruta se mostraba siempre, sin importar la pestaña activa

El `<div id="mapRuta">` (el mapa que dibuja la ruta calculada) había quedado, por un
`</div>` faltante, **fuera de todos los paneles** del dashboard del cliente — era hijo
directo de `.main`, no de la pestaña "Nuevo envío". Como el sistema de pestañas solo
oculta elementos con clase `.panel`, ese mapa aparecía siempre, debajo de cualquier
pestaña que estuviera activa. Se movió dentro del panel "Nuevo envío", donde
corresponde.

### 3. Borrar un envío `ENTREGADO` con paquete asociado fallaba con error 500

`EnvioServiceImpl.eliminarEntregado` (usado por el repartidor para limpiar su historial)
intentaba borrar el `Envio` directamente, sin borrar antes su `Paquete` asociado
(`paquetes.envio_id` es una FK hacia `envios`). Cualquier envío entregado con paquete
—que es la gran mayoría— hacía fallar el borrado con
`DataIntegrityViolationException`. Se aplicó el mismo patrón que ya usaba
`EnvioServiceImpl.eliminarEnvio` (borrado del lado del cliente): desvincular y borrar el
paquete antes de borrar el envío.

## Configuración relevante

```properties
# application.properties
app.repartidor.meta-diaria=5
app.repartidor.plazo-entrega-horas=24
app.repartidor.max-intentos-entrega=3
```

```sql
-- tabla tarifas (precio_base = precio por km)
estandar     | 0.10
express      | 0.25
prioritario  | 0.50
```

## Pendientes / fuera de alcance

- El "Historial de entregas" y "Entregas completadas esta semana" solo consideran
  `ENTREGADO`; si se quisiera un historial de "eventos de ganancia" (incluyendo
  recogidas de envíos pagados por el remitente), habría que construir una vista nueva.
- La API key de Google Maps que quedó comentada en una versión anterior de
  `Cliente/dashboard.html` ya no existe en el HTML actual (se limpió al reescribir el
  archivo), pero si el equipo la usó en otro lado conviene revocarla en Google Cloud
  Console de todas formas.
- No se agregó ninguna interfaz para que un admin configure las tarifas desde la web —
  hoy se cambian directo en la base de datos o en el código.
