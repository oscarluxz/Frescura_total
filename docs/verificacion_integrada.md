# Verificación del ERP integrado — 4 de noviembre de 2026

## Alcance y origen

Revisión del enunciado completo aportado por Junior y del SQL oficial enlazado en
él. Base de las correcciones: `main`, commit
`5bf71a99ef02db9c5caefd5d8175d31b0a7341d7`. Rama de trabajo:
`fix/integrated-release-validation`. No se modificaron ramas de los compañeros,
ni se hizo merge a main/develop. Se conservaron los cuatro parches locales.

El original SQL descargado tiene SHA-256
`d1261f039aa022a46757e2bb2eea1f01e745f366b32a9c5a91625499d0525f61`.
Se mantuvieron sus nombres, tipos y relaciones; el consolidado añade la extensión
de idempotencia de mermas, evita el borrado de la base y reúne los datos del equipo.

## Correcciones

| Problema comprobado | Resultado implementado |
| --- | --- |
| OpenPDF 2.2.5 requería Java 21 | OpenPDF 2.0.5, compilación y ejecución verificadas con JDK 17 |
| Diez records en modelos y objetos internos | Clases tradicionales encapsuladas, conservando constructores y acceso a datos |
| Lectura SQL mediante split de punto y coma | Analizador de literales/comentarios; fixtures mínimos separados de la prueba del consolidado completo |
| Dos Scanner sobre System.in | Lector compartido entre Main e inventario y salida controlada al terminar la entrada |
| Factura sin NIT y etiquetas en inglés | Empresa consultada desde MySQL, NIT, dirección/contacto, cliente/dirección, fecha, detalle y totales en español |
| CUFE sin identidad empresarial ni fecha | SHA-256 de número, fecha, total y NIT; una fecha común en venta, factura y PDF; QR decodificado y contrastado |
| Aprobación/rechazo sin comprobación de rol | Sesión académica Operador/Inspector; el controlador exige Inspector |
| Rechazo sin baja del stock | Inspección, baja de todo el saldo, historial y resolución atómicos; rollback ante fallo inducido |
| Actualización de vencimientos solo manual | Revisión diaria simulada al navegar por Main, con fecha de Bogotá y opción manual adicional |
| Vencidos sin indicación de disposición | Bloqueados para ventas y marcados PENDIENTE DE BAJA en la consulta de inventario |
| Venta Registrada sin paso a despacho | Acción Logística 9: Registrada → En preparación; se rechazan repeticiones y otros estados |
| Disponibilidad validada fuera de la transacción de rutas | Revalidación con bloqueo de vehículo, conductor y pedidos; una sola reserva en carreras concurrentes |
| Dos entregas podían contar pendientes simultáneamente | Bloqueo de la ruta durante la entrega; el último pedido completa la ruta y libera recursos |
| El usuario no veía el ID necesario para despachar | El comprobante de consola muestra el ID de la venta |
| Proveedor 33 incumplía el dato sintético solicitado | Marcador académico exacto y excepción limitada a actualizar ese registro |
| UML parcial | PNG/SVG/DOT con las 25 clases/enums de los cuatro paquetes model y sus asociaciones |
| README de inventario describía integración pendiente | Instrucciones actualizadas al código integrado y pruebas reproducibles |
| Advertencia de hilo JDBC al salir de exec:java | Cierre explícito del trabajador de limpieza del driver al terminar la aplicación |

El [README de OpenPDF](https://github.com/LibrePDF/OpenPDF) documenta la compatibilidad
de la serie 2.0.x con Java 17. No se cambió el Java predeterminado del equipo.

## Compilación y pruebas

Entorno principal de comprobación: Eclipse Temurin **17.0.21+9**, Maven **3.9.16**,
MySQL Community Server **8.4.13** y Connector/J **8.4.0**. El servidor 8.4 se
obtuvo de la [descarga oficial de MySQL](https://dev.mysql.com/downloads/mysql/8.4.html?os=3)
y se ejecutó en `127.0.0.1:13362`, en un directorio independiente, sin instalar
ni reemplazar servicios de Windows. El archivo ZIP coincidió con el MD5 publicado:
`7b94489d5524dea9b27097068bea25a8`.

`mvn -B clean verify`: **41 pruebas, 0 fallos, 0 errores, 0 omitidas**.
Incluye validaciones, consola, roles, revisión diaria, fechas límite, cuarentenas,
inspecciones, mermas, idempotencia, fallos inducidos, concurrencia, trazabilidad,
metadatos de claves foráneas, PDF y QR, y carga completa del consolidado.
Se generó `target/erp-frescura-total-1.0-SNAPSHOT.jar`.

`docs/tools/verify-integration.ps1`: **14 escenarios aprobados, 0 fallidos**,
también con JDK 17 y MySQL 8.4.13. Se cargó todo el SQL en un esquema nuevo y se
comprobó proveedor/catálogo, marcador sintético, clientes, ingreso de lote e
historial, FEFO con dos lotes, descuento/IVA, PDF/QR/CUFE/fechas, bloqueo de lotes,
rollback por fallo de factura, dos ventas concurrentes, preparación por API y
ruta hasta entrega con trazabilidad. El avance de estados utilizó controladores
y repositorios, sin UPDATE manual de preparación. Además se verificaron carreras
de reserva del mismo vehículo/conductor, del mismo pedido con recursos diferentes,
y la entrega concurrente de dos pedidos de una ruta. Los errores provocados de
stock insuficiente y factura forman parte de las pruebas de rollback y aparecen
intencionalmente en el registro.

El mismo conjunto pasó también en el servidor de diagnóstico MySQL **26.7.0-er**,
puerto `13361`. La comprobación con MySQL 8 atiende la versión pedida en el enunciado.

Para repetir, siga el README con una base inicial nueva y vacía cuyo nombre empiece
por `erp_inventory_test_`. Las demás pruebas generan sus propios nombres aleatorios.
No se ejecuta el CREATE DATABASE/USE del consolidado contra la base comercial.
Los esquemas se conservan para inspección; no se eliminan datos del usuario.

## Datos y diagramas

La carga completa del DML pasó las claves foráneas y la ecuación de existencias:
`cantidad = cantidad_inicial - SUM(mermas) - SUM(ventas)`, sin saldos negativos.

| Tablas | Registros iniciales |
| --- | ---: |
| proveedores | 55 |
| productos / productos_proveedor | 50 / 50 |
| lotes_inventario | 80 |
| clientes / ventas / detalles_venta / facturas | 50 / 50 / 50 / 50 |
| registro_cuarentena / inspecciones_calidad | 50 / 75 |
| registro_mermas / operaciones_merma | 50 / 50 |
| conductores / vehiculos | 50 / 50 |
| rutas_entrega / asignaciones_entrega / ventas_ruta | 19 / 19 / 38 |

El enunciado pide al menos 50 en las tablas principales y da como ejemplos
proveedores, productos, clientes, lotes y ventas; todas esas tablas cumplen.
Los catálogos de estados tienen su cantidad propia. La promoción académica está
preconfigurada en `SaleAllocationPolicy`; no depende de filas en promociones.

ER: **23 tablas, 156 atributos, 24 relaciones**, generado desde el DDL.
UML: **25 clases/enums, 27 asociaciones**, generado a partir de atributos Java,
con referencias por objeto y por identificador diferenciadas. Se revisó el PNG;
los SVG permiten ampliar el contenido sin perder legibilidad.

## Entrega y límites externos

Se verificó acceso GitHub como `jrssj`, con permisos de lectura/escritura, en el
repositorio privado `oscarluxz/Frescura_total`. Los colaboradores consultados fueron
`jrssj`, `oscarluxz` y `ThomasSerranoJacome`; no se dispone del usuario del trainer.
El propietario debe añadirlo o comprobar su acceso antes de entregar y comunicar
el hash completo del commit elegido. Las correcciones no se han mezclado con main.

La autenticación, una revisión en segundo plano y la validación fiscal real de la
DIAN no se implementaron: el enunciado permite simular la revisión diaria y el
CUFE/QR; los roles de esta consola se declaran expresamente como simulación.
No se automatizó la interfaz de NetBeans. La validación se hizo con Maven/JDK 17,
MySQL 8 y el recorrido de negocio. No se ejecutaron pruebas en la base comercial
del usuario. Una prueba satisfactoria acredita estos escenarios, no la ausencia
absoluta de defectos ni una calificación garantizada.
