# Verificación del registro de lotes

## Estado encontrado

La rama `feature/core-inventory-quality` ya contenía el avance JDBC en
`ca5b4124db50e06b5a93d9ba9934df34de5c4852`, posterior a configuración Java/JDBC
(`e19d3f1`) y modelos (`8e3f622`). Esta entrega corrige ese avance sin duplicarlo.

Se revisaron las ramas remotas del equipo:

| Rama | Commit revisado | Responsable |
| --- | --- | --- |
| `feature/suppliers-logistics` | `3b6c488` | Oscar |
| `feature/sales-billing-dian` | `cea91e7` | Thomas |
| `develop`, `main` | `384ed14` | Base Maven |

No había clases de inventario equivalentes en esas ramas. Hay diferencias que
deben acordarse al integrar: Oscar también usa
`frescuratotal.erp.common.DatabaseConnection` con configuración propia; Thomas usa
el prefijo `com.frescuratotal.erp`. No se realizó ningún merge.

## Corrección

El SQL disponible en `ERP_FrescuraTotal_Junior.zip`, entrada
`frescura-total-junior/docs/database.sql`, define `numero_lote` y `estante`.
Las consultas tenían tildes en esos nombres y fallarían al insertar. Se ajustaron
INSERT y UPDATE, y se renombraron los métodos `validateAmount` y
`validateSupplierProduct` a identificadores en inglés.

El `Main` ahora abre la consola de inventario. La consola también permite terminar
la entrada sin lanzar una excepción ni guardar formularios incompletos.

El ZIP es la fuente SQL recuperada de Descargas; no había un archivo SQL original
adjunto recuperable en el chat. Si el docente proporciona otra versión, debe
compararse antes de usar una base real.

## Ejecución

Requisitos: JDK 17, Maven y MySQL. Configure `ERP_DB_PASSWORD` y, según su entorno,
`ERP_DB_USER`, `ERP_DB_HOST`, `ERP_DB_PORT`, `ERP_DB_NAME` o `ERP_DB_URL`.
No coloque contraseñas en el código ni en Git.

```powershell
mvn test
mvn package
mvn org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

La clase principal es `frescuratotal.erp.Main`. El producto, el proveedor y su
relación en `productos_proveedor` deben existir antes de registrar un lote.

## Pruebas

`mvn test` ejecuta validaciones y consola. La prueba MySQL se omite si no está
definida `ERP_TEST_SCHEMA_FILE`; una compilación sin esa variable no demuestra
persistencia real.

Para la prueba de integración, use una instancia local de prueba y una base nueva,
vacía y con nombre que empiece por `erp_inventory_test_`. Configure `ERP_DB_URL`
para esa base, las credenciales de prueba y `ERP_TEST_SCHEMA_FILE` con la ruta al
SQL extraído del ZIP. La prueba ejecuta únicamente las sentencias CREATE TABLE e
INSERT del esquema; omite DROP DATABASE, CREATE DATABASE y USE. Deja los datos de
prueba en esa base, por lo que debe utilizar otra base vacía para repetirla.

La prueba comprueba cantidades y precios DECIMAL(10,2), ubicación normalizada,
fecha de vencimiento, código definitivo único, consulta dinámica del estado
Disponible, rechazo de proveedor no asociado y rollback después de un INSERT
cuando un trigger provoca el fallo del UPDATE.

La verificación de esta entrega se ejecutó en una instancia MySQL independiente,
ligada a `127.0.0.1:13361`, usando ese esquema. Resultado: 11 pruebas ejecutadas,
cero fallos, cero errores y ninguna omitida. Los casos de validación incluyen ID
inválido, fecha nula/vencida, cantidad nula/no positiva, precios nulos/negativos,
desbordamiento o exceso de decimales y ubicación demasiado larga.

Comando final: `mvn -B clean verify`, con Eclipse Temurin **17.0.21+9**,
Maven **3.9.16**, MySQL Community Server **26.7.0-er** y Connector/J **8.4.0**.
Se generó `target/erp-frescura-total-1.0-SNAPSHOT.jar`. Esta prueba acredita el
esquema recuperado y esas versiones; no se ejecutó contra la base de trabajo del
usuario ni contra otro servidor MySQL. El JDK 17 se descargó en la carpeta de
pruebas y se verificó su SHA-256 con los metadatos de Eclipse Adoptium, sin
modificar el Java predeterminado del equipo.
