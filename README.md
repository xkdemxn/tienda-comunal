# Sistema de Inventario y Ventas para Tienda

Sistema backend con Spring Boot para gestionar inventario, ventas, compras a
proveedores, tasas de cambio y reportes, con reduccion de stock mediante
escaneo de codigo de barras desde la camara del celular.

## Stack

- **Backend:** Spring Boot 3.3, Java 17, Spring Security + JWT, Spring Data JPA
- **Base de datos:** MySQL
- **Frontend de escaneo:** HTML + JS puro con la libreria `html5-qrcode`
  (servido por el propio Spring Boot en `/scanner.html`)
- **Despliegue sugerido:** Railway (o cualquier plataforma con soporte Docker)

## Estructura del proyecto

```
src/main/java/com/tienda/inventario/
├── config/          # Seguridad, CORS, seeder de roles
├── controller/       # Endpoints REST
├── dto/               # Objetos de entrada/salida de la API
├── entity/            # Entidades JPA (tablas)
├── enums/             # RolNombre, TipoMovimiento
├── exception/         # Manejo global de errores
├── repository/        # Interfaces JPA
├── security/          # JWT (filtro, util, UserDetails)
└── service/           # Logica de negocio
src/main/resources/
├── application.properties
└── static/scanner.html   # Frontend de escaneo con camara
```

## Como correrlo en local

1. Crea una base de datos MySQL local llamada `tienda_inventario`
2. Ajusta `src/main/resources/application.properties` si tu usuario/password
   de MySQL son distintos a `root`/`root` (o usa las variables de entorno,
   ver mas abajo)
3. Ejecuta:
   ```bash
   mvn spring-boot:run
   ```
4. La app queda en `http://localhost:8080`
5. Abre `http://localhost:8080/scanner.html` en el navegador del celular
   (debe estar en la misma red que tu compu, o usa un tunel como ngrok para
   probar la camara desde el celular apuntando a tu compu)

> **Importante:** los navegadores solo permiten acceder a la camara en
> conexiones **HTTPS** o en `localhost`. Para probar desde el celular en tu
> red local sin HTTPS, la camara no va a funcionar; usa ngrok o despliega
> directamente en Railway (que ya da HTTPS automatico).

## Variables de entorno (usadas en produccion / Railway)

| Variable | Descripcion | Ejemplo |
|---|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC de MySQL | `jdbc:mysql://host:3306/railway` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password de MySQL | *(la que te da Railway)* |
| `PORT` | Puerto de la app (Railway lo inyecta solo) | `8080` |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | *(genera una larga y aleatoria)* |
| `JWT_EXPIRATION_MS` | Duracion del token en milisegundos | `86400000` (24h) |
| `EXCHANGE_API_URL` | API externa de tasas de cambio | `https://api.exchangerate-api.com/v4/latest/USD` |
| `MONEDA_BASE` | Moneda base de tu tienda | `USD` |

**Nunca subas contraseñas reales al repositorio.** Todas estas variables se
configuran directamente en el panel de Railway (Settings → Variables), no en
el codigo.

## Despliegue en Railway (resumen)

1. Sube este proyecto a un repositorio de GitHub
2. En Railway: **New Project → Deploy from GitHub repo**
3. Railway detecta el `Dockerfile` y lo usa para compilar y correr la app
4. Agrega un plugin de **MySQL** dentro del mismo proyecto de Railway
5. En la pestaña **Variables** de tu servicio, agrega las variables de la
   tabla de arriba. Para las de MySQL, Railway te da variables propias
   (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`);
   arma `SPRING_DATASOURCE_URL` con ellas, por ejemplo:
   ```
   jdbc:mysql://${{MYSQLHOST}}:${{MYSQLPORT}}/${{MYSQLDATABASE}}
   ```
6. Railway te da un dominio publico con HTTPS automatico
   (`tuapp.up.railway.app`) — ahi mismo funcionara la camara sin problema

## Flujo de uso

1. **Registro/login** (`POST /api/auth/registro`, `POST /api/auth/login`) —
   el primer usuario admin debes crearlo indicando `"rol": "ADMIN"` en el
   registro
2. **Cargar productos** con su codigo de barras (`POST /api/productos`)
3. Abrir `/scanner.html` desde el celular, iniciar sesion, y escanear
   productos: cada escaneo los agrega al carrito
4. **Confirmar venta** — reduce el stock automaticamente y calcula el total
   (y su conversion a otra moneda si se eligio una)
5. Revisar **reportes** (`GET /api/reportes/ventas?desde=...&hasta=...`) y
   **stock bajo** (`GET /api/productos/stock-bajo`)

## Endpoints principales

| Metodo | Ruta | Rol requerido | Descripcion |
|---|---|---|---|
| POST | `/api/auth/registro` | Publico | Crear usuario |
| POST | `/api/auth/login` | Publico | Iniciar sesion, devuelve JWT |
| GET/POST/PUT/DELETE | `/api/productos/**` | ADMIN (GET tambien VENDEDOR) | CRUD de productos |
| GET | `/api/escaneo/{codigoBarras}` | Autenticado | Consultar producto por codigo de barras |
| POST | `/api/ventas` | ADMIN, VENDEDOR | Registrar venta (reduce stock) |
| POST | `/api/compras` | ADMIN | Registrar compra a proveedor (aumenta stock) |
| POST | `/api/inventario/ajuste` | ADMIN | Ajuste manual de stock (mermas, correcciones) |
| GET | `/api/reportes/ventas` | ADMIN | Reporte de ventas y productos mas vendidos |
| GET | `/api/productos/stock-bajo` | ADMIN | Productos por debajo del stock minimo |

## Decisiones de diseno importantes

- **Todo cambio de stock pasa por `InventarioService`**, que registra cada
  movimiento en la tabla `movimientos_inventario` (kardex) — asi hay
  trazabilidad completa de por que cambio el stock de cada producto
- **Control de concurrencia optimista** (`@Version` en `Producto`): si dos
  ventas intentan modificar el mismo producto al mismo tiempo, una de ellas
  recibe un error claro para reintentar, evitando que el stock quede
  inconsistente
- **La tasa de cambio se cachea** en base de datos y se actualiza cada 6
  horas con un job programado, en vez de consultar la API externa en cada
  venta
- **El escaneo de codigo de barras ocurre en el navegador** (JS, libreria
  `html5-qrcode`), no en el backend — el backend solo recibe el codigo ya
  leido y busca el producto

## Pendiente / mejoras sugeridas a futuro

- Paginacion en los listados de productos/ventas cuando crezca el catalogo
- Endpoint para editar/anular una venta (con su respectiva reversion de stock)
- Tests unitarios para los servicios criticos (`InventarioService`, `VentaService`)
- Dashboard visual (grafico) en vez de solo el JSON de reportes
