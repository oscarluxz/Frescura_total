# ERP para la Bodega de Alimentos "Frescura Total"

## Descripción

Sistema ERP de consola para la bodega de alimentos Frescura Total. Resuelve el problema de controlar en un solo lugar el inventario por lotes y su calidad, las ventas y facturación, el abastecimiento con proveedores y la logística de entrega. Permite saber qué producto hay, de qué proveedor viene y cuándo vence, y seguir cada pedido desde su preparación hasta que llega al cliente. Está hecho en Java con arquitectura MVC por componentes y una base de datos MySQL.

## Autores

- Jofran Junior Rodriguez Quauro — Inventario y calidad
- Thomas Serrano Jacome — Ventas y facturación
- Oscar Diego Corredor Camargo — Proveedores y logística

## Requerimientos previos

- Java 17
- Apache NetBeans IDE 12 o superior
- Apache Maven (incluido en NetBeans)
- MySQL 8
- Un cliente de MySQL para ejecutar el script (por ejemplo DBeaver o MySQL Workbench)

## Configuración de la base de datos

1. Inicie el servidor de MySQL en su equipo.
2. Cree la base de datos:

   ```sql
   CREATE DATABASE erp_frescura_total;
   ```

3. Ejecute el script `docs/database.sql` sobre esa base de datos. Este script crea todas las tablas (DDL) y carga los datos de prueba (DML).
4. Las credenciales se configuran en la clase `src/main/java/frescuratotal/erp/common/DatabaseConnection.java`, que las lee de variables de entorno. Valores de ejemplo:

   | Variable | Obligatoria | Valor de ejemplo |
   |---|---|---|
   | `ERP_DB_PASSWORD` | Sí | `mi_clave_de_mysql` |
   | `ERP_DB_USER` | No (por defecto `root`) | `root` |
   | `ERP_DB_HOST` | No (por defecto `localhost`) | `localhost` |
   | `ERP_DB_PORT` | No (por defecto `3306`) | `3306` |
   | `ERP_DB_NAME` | No (por defecto `erp_frescura_total`) | `erp_frescura_total` |

   La contraseña no está escrita en el código. Defina las variables en el sistema operativo antes de iniciar NetBeans; reinicie el IDE si ya estaba abierto. `ERP_DB_URL` permite indicar una URL JDBC completa y tiene prioridad sobre host, puerto y nombre.

## Instalación y ejecución

1. Clone el repositorio:

   ```
   git clone https://github.com/oscarluxz/Frescura_total.git
   ```

2. Abra NetBeans y use **File → Open Project**, eligiendo la carpeta clonada.
3. Compile con **Run → Clean and Build Project**. Debe terminar en `BUILD SUCCESS`.
4. Ejecute con **Run → Run Project** o presionando **F6**. Aparece el menú principal en la consola.

Opcional, desde la terminal y con la variable `ERP_DB_PASSWORD` definida:

```
mvn clean verify
mvn exec:java
```

## Estructura del proyecto

Arquitectura Modelo-Vista-Controlador organizada por componentes. Cada módulo de negocio tiene sus propios paquetes.

```
src/main/java/frescuratotal/erp/
├── common/            // Conexión a la base de datos (DatabaseConnection)
├── inventory/         // Inventario y calidad (modelo, controlador, vista, acceso a datos)
├── sales/             // Ventas y facturación
├── suppliers/
│   ├── model/         // Supplier, CatalogProduct, PurchaseRecord, SupplierReliability
│   ├── repository/    // Interfaces y clases JDBC
│   ├── controller/    // SupplierController
│   ├── analytics/     // SupplierAnalyticsService: reporte de rendimiento con Stream API
│   └── view/          // SupplierView, SupplierAnalyticsView
├── logistics/
│   ├── model/         // Vehicle, Driver, DeliveryRoute, RouteStop, DispatchOrder y enums de estado
│   ├── repository/    // Interfaces y clases JDBC
│   ├── controller/    // LogisticsController
│   └── view/          // LogisticsView
└── Main.java          // Punto de entrada y menú principal
docs/
├── database.sql             // DDL y datos de prueba
├── diagrama_er.png          // Diagrama entidad-relación
├── diagrama_clases.png      // Diagrama de clases UML del proyecto completo
├── diagrama_clases_*.png    // Detalle por módulo (inventario, proveedores, logística, ventas)
├── patrones_proveedores.md  // Patrones del módulo de proveedores
└── patrones_logistica.md    // Patrones del módulo de logística
```

El modelo guarda los datos y sus reglas propias, la vista solo lee e imprime por consola, y el controlador aplica las reglas de negocio. El acceso a MySQL queda en los repositorios.

## Principios y patrones de diseño

**Singleton.** `DatabaseConnection` tiene una única instancia que centraliza la configuración de la conexión. Se eligió para que todo el equipo use una sola fuente de configuración y la contraseña no se repita en el código de cada módulo.

**Repository / DAO.** En los módulos de proveedores y logística cada entidad tiene una interfaz y una implementación JDBC (por ejemplo `SupplierRepository` y `JdbcSupplierRepository`). Todo el SQL vive en las implementaciones. Se eligió para separar la lógica de negocio del acceso a datos, de modo que un cambio en una tabla solo afecta una clase.

**Principios SOLID.** Responsabilidad única: cada clase tiene una sola función (modelo, repositorio, controlador o vista). Inversión de dependencias: los controladores reciben sus repositorios por constructor y dependen de interfaces, y el ensamblado se hace en `Main`. Segregación de interfaces: los repositorios de solo lectura, como el historial de compras, están separados de los que modifican datos.

**Máquina de estados y transacciones (logística).** El enum `DispatchStatus` solo permite avanzar un pedido de En preparación a En tránsito y luego a Entregada. Las operaciones que modifican varias tablas se ejecutan con `commit` y `rollback` para no dejar la base de datos a medias.

El detalle de cada decisión está en `docs/patrones_proveedores.md` y `docs/patrones_logistica.md`.

**Adaptador y fábrica (ventas e inventario).** Ventas no conoce las clases de inventario: depende de la interfaz `LotSaleValidator`. La clase `InventoryLotSaleValidator` adapta esa interfaz a `InventoryQualityDAO`, y `SalesModuleFactory` ensambla el módulo. Se eligió así para que cada módulo cambie sin romper al otro (inversión de dependencias) y para que la regla de calidad del inventario exista en un solo lugar.

**Política de asignación FEFO (ventas).** `SaleAllocationPolicy` concentra la regla de qué lote se vende primero y cuándo aplica el descuento, separada del SQL y de la consola, lo que permite probarla con pruebas automáticas. Cada venta se guarda en una sola transacción con `commit` y `rollback`.

El detalle del módulo de ventas está en `docs/README_ventas.md`.

## Recorrido del sistema integrado

1. Al entrar al menú principal se revisan los vencimientos automáticamente, una vez por fecha de Bogotá durante la sesión. Es la revisión diaria **simulada** permitida por el enunciado; no requiere un servicio externo. La opción 2 de inventario permite repetirla manualmente.
2. Inventario permite registrar lotes, consultar existencias y alertas, ponerlos en cuarentena y registrar mermas. Un vencido con existencias se muestra como **PENDIENTE DE BAJA** y nunca puede venderse.
3. La sesión inicia como Operador. Para inspeccionar, use la opción **5 del menú principal → 2, Inspector**. Es una simulación de roles para la actividad, sin autenticación de usuarios. El controlador rechaza aprobaciones o rechazos desde una sesión Operador. La aprobación resuelve la cuarentena; el rechazo registra automáticamente la baja de todas las existencias restantes, junto con la inspección, dentro de una sola transacción.
4. En **4, Ventas y facturación**, registre o seleccione un cliente y confirme una venta. Se aplican FEFO, descuento del 50 % a lotes próximos a vencer e IVA académico del 19 %. La factura toma los datos reales de `informacion_empresa` y del cliente e incluye fecha de emisión, NIT, detalle, totales, CUFE y QR simulados. Los archivos quedan en `output/invoices/`.
5. En **3, Logística → 9**, indique el ID mostrado al confirmar la venta para pasar de Registrada a En preparación. El pedido aparece en el panel. Asigne vehículo y conductor a una ruta, inicie el recorrido y confirme la entrega. No es necesario editar estados directamente en SQL.

El CUFE académico se calcula como SHA-256 de `numeroFactura|fechaHoraISO|totalConDosDecimales|NIT`; esa fecha también se guarda en venta y factura. El QR contiene una URL simulada. No se envían documentos a la DIAN ni se afirma una habilitación fiscal real.

### Módulo de Analíticas

El **Reporte de Rendimiento de Proveedores** se abre desde el menú principal con la opción **6, Generar Reporte de Rendimiento de Proveedores**. Su propósito es dar al departamento de compras datos concretos para evaluar a cada proveedor, en lugar de decidir solo por percepción.

El reporte consulta todos los lotes del inventario con su proveedor y su estado, y calcula dos indicadores:

- **Proveedor con mayor volumen de suministro:** suma la cantidad inicial de todos los lotes de cada proveedor y muestra el que más producto ha entregado. Si hay empate, muestra cualquiera de los empatados.
- **Proveedores con lotes vencidos:** lista, sin nombres repetidos, los proveedores que tienen al menos un lote en estado *Vencido*.

**Cómo ayuda a la toma de decisiones:** el primer indicador identifica a los socios más importantes, con quienes conviene negociar mejores precios y condiciones por volumen. El segundo señala a los proveedores que pueden representar un riesgo para la calidad del inventario, para exigirles mejores fechas de vencimiento o revisar si se sigue comprándoles. Si todavía no hay lotes registrados, el sistema muestra "No hay datos de inventario suficientes para generar estadísticas." en lugar de fallar.

**Decisión técnica:** el SQL vive en `JdbcSupplierLotRepository` (patrón Repository, igual que el resto del módulo) y los dos cálculos están en `SupplierAnalyticsService`, implementados únicamente con la API de Streams (`groupingBy`, `reducing`, `max`, `filter`, `distinct`). El servicio recibe el repositorio por el constructor, por eso se prueba sin base de datos en `SupplierAnalyticsServiceTest`.

## SQL, datos y diagramas

El DDL se contrastó con el [SQL oficial de la actividad](https://drive.google.com/file/d/1pvak9x6hoFD2jkSmy7avQKkKqpOffgcm/view). Su SHA-256 es `d1261f039aa022a46757e2bb2eea1f01e745f366b32a9c5a91625499d0525f61`. Se conservan las 22 tablas originales y se añade `operaciones_merma`, para evitar descontar dos veces la misma pérdida al reintentar. El consolidado no elimina datos existentes; se utiliza una base vacía para una instalación nueva. No vuelva a ejecutarlo sobre una base ya poblada.

El consolidado contiene 55 proveedores, 50 productos, 80 lotes y 50 clientes, ventas, detalles y facturas, con referencias y existencias verificadas. El mínimo del enunciado corresponde a las tablas principales; los catálogos de estados y la información de empresa tienen sus cantidades propias. El proveedor 33 conserva el marcador `SYN-FT-900427-AG`, solicitado expresamente para datos sintetizados por la actividad. Se admite únicamente al actualizar ese registro; no es un NIT fiscal real ni una regla general de validación.

`docs/diagrama_er.png` muestra las 23 tablas, atributos y 24 relaciones. `docs/diagrama_clases.png` cubre las 25 clases y enums de los cuatro paquetes `model`. Ambos incluyen SVG ampliable y fuente DOT. Para regenerarlos, desde `docs/tools`: `npm ci` y `npm run generate`. Node.js se necesita solo para regenerar documentación, no para ejecutar el ERP.

Se conservan también los diagramas por módulo aportados por el equipo y su diagrama general anterior en `docs/diagrama_clases_equipo.png`. El diagrama global regenerable refleja las clases de la versión corregida.

## Pruebas y entrega

`mvn clean verify` ejecuta las pruebas de validación y consola. Las que requieren MySQL se omiten si no se define `ERP_TEST_SCHEMA_FILE`; ese resultado por sí solo no acredita persistencia. Para habilitarlas utilice un servidor local dedicado, una base **vacía y nueva** cuyo nombre comience por `erp_inventory_test_`, y una cuenta con permisos para crear bases de prueba:

```powershell
$env:ERP_DB_URL = 'jdbc:mysql://127.0.0.1:13362/erp_inventory_test_primera_ejecucion?createDatabaseIfNotExist=true&serverTimezone=America/Bogota'
$env:ERP_DB_USER = 'usuario_pruebas'
$env:ERP_DB_PASSWORD = 'clave_pruebas'
$env:ERP_TEST_SCHEMA_FILE = (Resolve-Path docs/database.sql).Path
mvn clean verify
```

Para repetir, cambie el nombre de la base inicial por uno nuevo. Las pruebas conservan sus esquemas para inspección y omiten las instrucciones CREATE DATABASE/USE del archivo oficial. Comprueban transacciones, rollback, concurrencia, inspecciones, mermas, referencias y el DML consolidado completo. El recorrido adicional de los cuatro módulos se ejecuta con:

```powershell
./docs/tools/verify-integration.ps1 -TestServerUrl 'jdbc:mysql://127.0.0.1:13362/'
```

El script crea un nombre de prueba nuevo; no requiere modificar estados a mano. Consulte resultados y límites en `docs/verificacion_integrada.md`. La interfaz de NetBeans debe abrirse con JDK 17 y las mismas variables; la compilación comprobada se realizó mediante Maven.

Para la entrega final, el propietario debe comprobar el acceso del trainer como colaborador del repositorio privado y comunicar el hash completo del commit elegido (`git rev-parse HEAD`). Las correcciones de `fix/integrated-release-validation` están integradas en `main`, junto con los diagramas aportados posteriormente por el equipo.
