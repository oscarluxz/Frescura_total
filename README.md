# ERP Frescura Total — Core, Inventarios y Calidad

Módulo de Junior, independiente en `feature/core-inventory-quality`.
Java 17, Maven, MySQL y arquitectura MVC con POO clásica, encapsulación,
colecciones, excepciones, JDBC, Singleton y separación de responsabilidades.
No utiliza frameworks de persistencia ni records.

## Funcionalidades

| Opción | Función |
| --- | --- |
| 1 | Registrar lote con proveedor, producto, precios, cantidades y ubicación |
| 2 | Persistir estados según vencimiento, conservando cuarentenas |
| 3 | Consultar existencias, vencimientos y estados |
| 4 | Registrar cuarentena con motivo y responsable |
| 5 | Registrar inspección aprobada o rechazada con inspector y notas |
| 6 | Registrar merma con transacción y clave para reintentos |
| 7 | Rastrear proveedor original y clientes con entregas del lote |
| 8 | Consultar lotes de un proveedor |
| 9 | Consultar lotes recibidos por un cliente |
| 10 | Consultar historial de inspecciones |

Los nombres Java y comentarios están en inglés; la consola, documentación y SQL
de la actividad están en español. Las tablas mantienen sus nombres originales.

## Base de datos

Para una instalación **nueva**, ejecutar `docs/database.sql` desde MySQL Workbench
o el cliente MySQL. Incluye 23 tablas, sus catálogos iniciales y 24 claves
foráneas. No incluye DROP DATABASE ni datos comerciales de prueba.

Si ya existe el esquema de la actividad, **no volver a cargar el archivo completo**:
seleccionar la base existente y ejecutar únicamente
`docs/migrations/001_waste_operations.sql`. Esa extensión crea una tabla con la
clave única de cada baja y su relación con `registro_mermas`; no elimina ni
modifica tablas o registros existentes. Es necesaria antes de registrar mermas.

El SQL base se recuperó de `ERP_FrescuraTotal_Junior.zip`, ruta interna
`frescura-total-junior/docs/database.sql`. Se conservaron sus tablas, tipos,
relaciones y catálogos; se retiró el borrado de la base y se añadió la extensión
de mermas. El chat no contenía un original del docente recuperable: si se recibe
otra versión oficial, comparar antes de integrarla.

El diagrama `docs/diagrama_er.png` incluye atributos, tipos, PK, FK, UQ,
nulabilidad y cardinalidades del DDL. También hay SVG ampliable y fuente DOT.
Los cardinales representan restricciones reales del esquema, no supuestos de
negocio adicionales. Las tablas de inventario aparecen en verde.

## Configuración y ejecución

Requisitos: JDK 17, Maven y un servidor MySQL con el esquema instalado. Antes de
registrar lotes deben existir un producto, su proveedor y la relación
`productos_proveedor`; esos datos se gestionan con el equipo de proveedores.

Definir en el entorno `ERP_DB_PASSWORD` y, según el equipo:

| Variable | Valor predeterminado |
| --- | --- |
| `ERP_DB_HOST` | `localhost` |
| `ERP_DB_PORT` | `3306` |
| `ERP_DB_NAME` | `erp_frescura_total` |
| `ERP_DB_USER` | `root` |
| `ERP_DB_URL` | Opcional; reemplaza host, puerto y nombre |

La contraseña no tiene valor predeterminado. No escribirla en Java, en la URL ni
en archivos versionados. El programa lee el entorno del proceso; no carga `.env`
automáticamente. Para conexiones personalizadas usar la zona `America/Bogota`.

Desde la raíz del proyecto:

```powershell
mvn clean verify
mvn exec:java
```

La clase principal es `frescuratotal.erp.Main`; el módulo también tiene la entrada
`frescuratotal.erp.inventory.view.InventoryConsoleView`. El JAR generado en
`target` no contiene sus dependencias: usar Maven para ejecutarlo con Connector/J.

## NetBeans

1. Abrir **File > Open Project** y seleccionar la carpeta que contiene `pom.xml`.
2. Registrar un JDK 17 en **Tools > Java Platforms**, si no está disponible.
3. Elegir ese JDK para el proyecto y para la ejecución de Maven. El POM fija
   `maven.compiler.release=17` y las versiones de compilación, pruebas y ejecución.
   `.mvn/jvm.config` fija UTF-8 para que las tildes de la consola sean legibles.
4. Definir las variables de conexión antes de iniciar NetBeans, o configurarlas
   en su entorno de ejecución. Reiniciar NetBeans si estaba abierto cuando se
   cambiaron las variables.
5. Ejecutar **Clean and Build**. Para la consola, ejecutar `Main.java`, o agregar
   la acción Maven `exec:java` del proyecto. Se mostrará el menú de inventario.

En este equipo se verificó con Temurin 17.0.21+9 descargado en
`C:\Users\jr846\Documents\Codex\2026-11-03\referenced-chatgpt-conversation-this-is-an\work\java17\jdk-17.0.21+9`.
No se cambió el Java predeterminado del sistema. La verificación se hizo por Maven;
no se automatizó la interfaz de NetBeans.

## Reglas de negocio

- Vence hoy o antes: `EXPIRED` / `Vencido`, bloqueado para ventas.
- Vence entre mañana y siete días: `NEAR_EXPIRY` / `Proximo a Vencer`.
- Vence después de siete días: `AVAILABLE` / `Disponible`.
- `IN_QUARANTINE` / `En Cuarentena` prevalece sobre las fechas hasta una aprobación.
- Una aprobación resuelve la cuarentena y calcula otra vez el estado por fecha.
  Un lote vencido permanece bloqueado aunque la inspección sea aprobada.
- Un rechazo queda en el historial y conserva la cuarentena. No descuenta stock
  automáticamente: la pérdida se registra una sola vez mediante una merma.
- Toda merma valida cantidad positiva, máximo dos decimales y stock suficiente.
  Bloquea la fila del lote y guarda descuento, historial y clave en una transacción.
- Repetir la misma clave y los mismos datos devuelve el registro anterior. Usar
  esa clave con otros datos se rechaza. Cada inspección rechazada vigente admite
  una baja; las inspecciones antiguas resueltas no sirven para otra baja.
- Para mermas sin inspección, el responsable debe conservar la clave de la misma
  pérdida. Dos claves distintas representan dos operaciones distintas.
- Una pérdida parcial de un lote en cuarentena no libera el resto del lote;
  requiere inspección. Un lote sin existencias nunca pasa la validación de venta.
- La trazabilidad de clientes usa `detalles_venta -> ventas -> clientes` y solo
  ventas `Entregada`. No atribuye entregas a ventas registradas o canceladas.

## Pruebas reproducibles

`mvn clean verify` sin `ERP_TEST_SCHEMA_FILE` ejecuta 13 pruebas sin MySQL y omite
las 17 de integración. Ese resultado por sí solo no acredita la persistencia.

Para la prueba completa, preparar una instancia MySQL **de prueba**, nunca una
base comercial. La cuenta necesita permisos para crear bases de prueba. Configurar
`ERP_DB_URL` con una base vacía cuyo nombre empiece por `erp_inventory_test_`
(puede usarse `createDatabaseIfNotExist=true`), `ERP_DB_USER` y
`ERP_DB_PASSWORD`. Después:

```powershell
$env:ERP_TEST_SCHEMA_FILE = (Resolve-Path docs/database.sql).Path
# Para probar también la carga del DML, indicar el archivo real recibido de Oscar:
$env:ERP_TEST_SUPPLIERS_FILE = 'C:\ruta\dml_suppliers_logistics.sql'
mvn clean verify
```

Cada ejecución requiere una base inicial vacía con nombre nuevo. Las pruebas
adicionales crean sus propios esquemas aleatorios con ese prefijo. Se conservan
para inspección; ninguna prueba borra la base de trabajo del usuario. No se
ejecutan las instrucciones CREATE DATABASE/USE del archivo SQL sobre otra base.
En PowerShell antiguo, una variable con cadena vacía puede eliminarse: usar
PowerShell 7 o una contraseña no vacía para el usuario de pruebas.

Resultado completo comprobado: **30 pruebas, cero fallos, cero errores y cero
omitidas**, con JDK 17.0.21+9, Maven 3.9.16, MySQL Community 26.7.0-er y
Connector/J 8.4.0. Incluye fechas límite, bloqueos de venta, transacciones y
rollback, cuarentenas, inspecciones, referencias antiguas, reintentos concurrentes,
mermas simultáneas, trazabilidad, DDL y un recorrido completo del menú. No se
ejecutó contra la base comercial del usuario ni contra un servidor MySQL 8.x.

Los datos de integración se entregan en `docs/dml_inventory.sql`: solo INSERT,
IDs explícitos y dependencias compatibles con los proveedores del archivo de
Oscar. Consultar `docs/dml_inventory.md` para el orden de carga, los rangos de IDs,
los conteos y los ejemplos de lotes que puede referenciar el DML de ventas.
La prueba adicional de carga requiere `ERP_TEST_SUPPLIERS_FILE`; si no se define,
se omite esa prueba aunque se hayan habilitado las demás pruebas MySQL.

## Integración pendiente del equipo

El módulo de Junior está implementado y probado de forma independiente. El flujo
de ventas de Thomas debe llamar al control de inventario dentro de **su misma
conexión y transacción**, antes de descontar stock e insertar detalles:

```java
connection.setAutoCommit(false);
InventoryQualityDAO inventory = new InventoryQualityDAO();
inventory.assertSellable(connection, lotId, quantity);
// Deduct stock and insert sale details using this same connection.
// Commit all changes together, or roll back the entire sale on any failure.
```

`assertSellable` conserva el bloqueo del lote hasta commit/rollback. Rechaza
autocommit, vencidos, cuarentenas y cantidades insuficientes; no descuenta por sí
misma. Thomas debe agrupar cantidades por lote y bloquearlos por ID ascendente,
usar un UPDATE condicionado a stock suficiente y comprobar la fila afectada.
Una consulta previa en otra conexión no protege contra ventas concurrentes.
También debe conservar correctamente las relaciones de detalle y el estado de
venta para que la trazabilidad sea real. La integración de ventas no se declara
terminada ni se modificó su rama.

Oscar debe mantener proveedores, productos y su catálogo relacionado. Su rama usa
la misma ruta `frescuratotal.erp.common.DatabaseConnection` con otra configuración;
debe acordarse la implementación compartida al integrar. Thomas usa además el
prefijo `com.frescuratotal.erp`; debe unificarse con `frescuratotal.erp`.

Se revisaron las ramas de Oscar (`3b6c488`) y Thomas (`cea91e7`). No se modificaron.
No se hizo merge a `develop` ni a `main`. Al comenzar esta continuación, `main`
ya contenía un merge previo `c9260f1` realizado fuera de este trabajo.

## Regeneración opcional del diagrama

Solo para documentación, requiere Node.js; no es una dependencia del ERP Java.
Desde `docs/tools`, ejecutar `npm install` y `npm run generate`. El generador usa
el SQL de la carpeta `docs` y produce PNG, SVG y DOT; no consulta ni modifica MySQL.

## Archivos y estado de entrega

`docs/inventory-verification.md` registra la verificación inicial del tercer
avance; este README describe la entrega completa. Los cuatro archivos locales de
parche se conservaron sin añadirlos al historial. La traducción local del mensaje
de configuración JDBC se conserva e incorpora en esta entrega. Las credenciales,
bases de pruebas, JDK descargado y dependencias de generación no se versionan.

Consultar los commits reales con `git log --oneline 408a4e4..HEAD`. Las pruebas de
integración y los contratos de conexión/ventas son los puntos de comprobación
para la integración final del equipo; no hay fallos conocidos pendientes en el
módulo independiente tras la verificación indicada.
