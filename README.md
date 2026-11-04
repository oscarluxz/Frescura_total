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

   La contraseña no está escrita en el código, para no subirla al repositorio. En NetBeans se puede definir en las propiedades del proyecto, en **Run → Environment**, o en el sistema operativo antes de abrir el IDE.

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
mvn compile org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=frescuratotal.erp.Main
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
│   └── view/          // SupplierView
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
