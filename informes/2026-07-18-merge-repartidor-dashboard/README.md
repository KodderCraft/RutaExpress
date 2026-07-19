# Informe: fusión de `master` en `repartidor-dashboard` (2026-07-18)

## Resumen

Se fusionó `origin/master` dentro de la rama `repartidor-dashboard` para incorporar
los últimos cambios del equipo (panel de administrador, interfaz de cliente) antes
de seguir trabajando en la feature de entregas fallidas / devoluciones.

**El merge en sí no tuvo conflictos** (`git pull origin master --no-rebase` resolvió
todo con la estrategia `ort` automáticamente). Los 4 commits que trajo `master` tocan
archivos que no se solapan con los de esta rama:

- `AdminController.java` (nuevo)
- `application.properties`
- `RutaExpressApplication.java`
- `static/css/stilos.css`
- `templates/Cliente/dashboard.html`
- `templates/componentes/sidebarCliente.html`
- `templates/administrador/usuarios.html` (nuevo)

Sin embargo, **después del merge**, al levantar el proyecto y verificar la nueva
funcionalidad de reintentos/devolución (agregada en esta rama), aparecieron 2 errores
de entorno/esquema que bloquearon la ejecución, más 1 hueco funcional heredado de
`master` que quedó documentado pero sin resolver (fuera del alcance de esta rama).
Este informe detalla los tres.

---

## Error 1 — Contraseña de la base de datos no coincide con `application.properties`

### Síntoma

Al ejecutar `./mvnw spring-boot:run`, la aplicación fallaba al iniciar con:

```
org.postgresql.util.PSQLException: FATAL: password authentication failed for user "postgres"
...
Unable to determine Dialect without JDBC metadata
```

El contexto de Spring nunca llegaba a levantar el servidor (Tomcat se detenía
inmediatamente después de inicializarse).

### Causa raíz

`src/main/resources/application.properties` tiene:

```properties
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Pero el contenedor Docker real (`rutaexpress-db`, definido en `docker-compose.yml`)
fue inicializado con:

```yaml
POSTGRES_PASSWORD: "1231"
```

Postgres solo aplica `POSTGRES_PASSWORD` la primera vez que se crea el volumen de
datos — si el volumen ya existía (como en este caso, contenedor con 2 días de
antigüedad), cambiar el `.yml` no cambia la contraseña real ya almacenada.

Revisando el historial de `application.properties` (`git log -p`) se ve que este
valor **se ha alternado entre `1231`, `admin` y `postgres`** en distintos commits de
varios integrantes del equipo — cada quien lo ha estado ajustando a su propio entorno
local y subiendo el cambio sin darse cuenta.

### Solución aplicada

No se modificó el archivo versionado (para no romper el entorno de otro compañero
otra vez). Se levantó el servidor sobrescribiendo la contraseña solo para esta sesión
de verificación, vía variable de entorno:

```bash
SPRING_DATASOURCE_PASSWORD=1231 ./mvnw spring-boot:run
```

### Recomendación para el equipo

Este problema va a seguir repitiéndose mientras la contraseña esté hardcodeada y
versionada. Opciones a evaluar:

1. Sacar `spring.datasource.password` de `application.properties` y leerlo solo de
   variable de entorno (`${DB_PASSWORD}`), documentando en el `README.md` el valor
   que corresponde al `docker-compose.yml` del proyecto.
2. Si cada quien necesita su propio valor local, usar
   `application-local.properties` (ignorado por git) en vez de pisar el archivo
   compartido.

---

## Error 2 — Check constraint desactualizado bloquea el nuevo estado `DEVUELTO`

### Síntoma

Al probar el flujo de reintentos de entrega (ver `EnvioService.marcarNoEntregado`),
el **tercer intento fallido** — el que debía mover el envío al nuevo estado
`DEVUELTO` — respondía **HTTP 500**. El log mostraba:

```
WARN  org.hibernate.orm.jdbc.error : ERROR: new row for relation "envios"
      violates check constraint "envios_estado_check"
ERROR ... DataIntegrityViolationException: could not execute statement
```

### Causa raíz

La tabla `envios` tiene un *check constraint* que Hibernate generó automáticamente en
su momento para el enum `EstadoEnvio` (esto ocurre porque el proyecto usa
`@Enumerated(EnumType.STRING)` y `spring.jpa.hibernate.ddl-auto=update`, que crea
constraints de validación a nivel de base de datos para los enums):

```sql
CHECK (estado::text = ANY (ARRAY['PENDIENTE','RECOGIDO','EN_CAMINO','ENTREGADO','CANCELADO']::text[]))
```

Este constraint fue generado cuando `EstadoEnvio` solo tenía esos 5 valores. Al
agregar `DEVUELTO` al enum de Java, **`ddl-auto=update` agrega columnas y tablas
nuevas, pero nunca actualiza constraints ya existentes** — es una limitación conocida
de Hibernate en modo `update`. El código Java quedaba correcto, pero la base de datos
seguía rechazando el valor nuevo.

Importante: esto **solo afecta bases de datos que ya existían antes de este cambio**.
Una base nueva (volumen de Docker recién creado) generará el constraint correcto
desde cero, porque Hibernate lo construye a partir del enum actual.

### Solución aplicada

Se corrigió el constraint directamente en la base de datos local de desarrollo:

```sql
ALTER TABLE envios DROP CONSTRAINT envios_estado_check;
ALTER TABLE envios ADD CONSTRAINT envios_estado_check
  CHECK (estado::text = ANY (ARRAY['PENDIENTE','RECOGIDO','EN_CAMINO','ENTREGADO','CANCELADO','DEVUELTO']::text[]));
```

**Este proyecto no usa Flyway ni Liquibase**, así que este fix no quedó registrado en
ningún archivo de migración — vive únicamente en el volumen de Docker de esta
máquina.

### Recomendación para el equipo

**Cualquier compañero con su propia base de datos local ya inicializada va a
encontrarse el mismo error 500** en cuanto pruebe un envío con 3 intentos fallidos.
Dos formas de resolverlo:

1. Correr el mismo `ALTER TABLE` de arriba contra su base local.
2. Más simple: borrar el volumen de Docker y dejar que se recree desde cero
   (`docker compose down -v && docker compose up -d`) — pierde los datos de prueba
   locales, pero el esquema queda generado correctamente desde el enum actual.

A futuro, si el equipo agrega más valores a enums existentes (`EstadoEnvio`,
`EstadoPago`, `EstadoIncidencia`), va a volver a pasar esto mismo. Vale la pena
considerar adoptar Flyway/Liquibase para que estos cambios de esquema queden
versionados y se apliquen de forma consistente en todas las máquinas.

---

## Hallazgo adicional (no corregido, fuera de alcance) — `AdminController` referencia una plantilla inexistente

Al revisar los cambios que trajo `master`, se detectó que `AdminController.java`
(agregado en `master`) tiene:

```java
@GetMapping("/administrador/pagos")
public String pagos(Model model){
    model.addAttribute("pagos", pagoRepository.findAll());
    return "administrador/pagos";
}
```

Pero **`src/main/resources/templates/administrador/pagos.html` no existe** —
tampoco existen `administrador/envios.html` ni `administrador/incidencias.html`,
aunque sus respectivos métodos en `AdminController` sí están implementados.
Visitar cualquiera de esas tres rutas como administrador va a producir un error
`TemplateInputException` (500).

No se corrigió como parte de este trabajo porque no está relacionado con la feature
de entregas fallidas ni con esta rama — queda documentado aquí para que quien
retome el panel de administrador lo tenga en cuenta.

---

## Checklist rápido para otros programadores

- [ ] Si tu servidor no arranca con `password authentication failed for user
      "postgres"`: usa `SPRING_DATASOURCE_PASSWORD=1231` al levantar (o el valor que
      tenga tu `docker-compose.yml` local), no edites `application.properties`.
- [ ] Si al marcar una entrega como "no entregada" por tercera vez te da error 500
      por `envios_estado_check`: corre el `ALTER TABLE` de la sección "Error 2" contra
      tu base local, o recrea el volumen de Docker.
- [ ] Si vas a trabajar en el panel de administrador: `administrador/pagos.html`,
      `administrador/envios.html` e `administrador/incidencias.html` no existen
      todavía — créalas antes de exponer esos enlaces a un usuario real.
