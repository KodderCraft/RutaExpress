# 🚚 RutaExpress

> Sistema web de gestión de entregas (Delivery) desarrollado como proyecto de aprendizaje.

## 📖 Descripción

**RutaExpress** es una aplicación web desarrollada con fines educativos para aprender el desarrollo de aplicaciones empresariales utilizando **Spring Boot**, **Java** y **PostgreSQL**.

El proyecto simula el funcionamiento de una empresa de entregas, permitiendo administrar clientes, repartidores, pedidos y rutas desde una plataforma web.


---

# 🎯 Objetivos

- Aprender el desarrollo Backend con Spring Boot.
- Consumir una base de datos PostgreSQL.
- Aplicar buenas prácticas de programación.
- Crear una API REST.
- Integrar el backend con una aplicación web.

---

# 🛠 Tecnologías

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- Hibernate
- HTML5
- CSS3
- JavaScript
- Bootstrap
- Git
- GitHub

---

# 📂 Estructura del proyecto

```
RutaExpress/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   ├── resources/
│   │   └── webapp/
│   │
│   └── test/
│
├── pom.xml
├── README.md
└── .gitignore
```

---

# 📌 Funcionalidades previstas

## Administración

- Gestión de usuarios
- Inicio de sesión
- Roles y permisos

## Clientes

- Registrar clientes
- Editar clientes
- Eliminar clientes
- Buscar clientes

## Repartidores

- Registrar repartidores
- Asignar pedidos
- Consultar disponibilidad

## Pedidos

- Crear pedidos
- Modificar pedidos
- Cancelar pedidos
- Historial de pedidos

## Direcciones

- Registrar direcciones
- Integración con mapas
- Coordenadas geográficas

## Seguimiento

- Estado del pedido
- Pedido pendiente
- En ruta
- Entregado
- Cancelado

## Reportes

- Pedidos realizados
- Entregas completadas
- Estadísticas básicas

---

# 🗄 Base de datos

Motor:

```
PostgreSQL
```

Principales entidades:

- Usuario
- Cliente
- Repartidor
- Pedido
- Dirección
- Vehículo
- EstadoPedido

---

# 🚀 Instalación

## Clonar el proyecto

```bash
git clone https://github.com/usuario/RutaExpress.git
```

## Entrar al proyecto

```bash
cd RutaExpress
```

## Configurar PostgreSQL

Crear una base de datos:

```sql
CREATE DATABASE rutaexpress;
```

Editar el archivo:

```
application.properties
```

Configurar:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rutaexpress
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

## Ejecutar

```bash
mvn spring-boot:run
```

---

# 📈 Estado del proyecto

🚧 En desarrollo

---

# 📚 Propósito

Este proyecto forma parte de mi proceso de aprendizaje en el desarrollo de aplicaciones web con Java y Spring Boot.

Durante el desarrollo se pondrán en práctica conceptos como:

- Arquitectura MVC
- API REST
- Persistencia de datos
- Relaciones entre entidades
- Seguridad
- Validaciones
- Buenas prácticas de programación

---

# 📄 Licencia

Proyecto desarrollado únicamente con fines educativos.
