# Datos de integración de Junior

`dml_inventory.sql` contiene únicamente INSERT con IDs explícitos y datos ficticios.
Se verificó contra el DDL del proyecto y los proveedores de
`docs/dml_suppliers_logistics.sql` de Oscar, revisado en `0afa3d3`.

## Orden de carga

1. El esquema y sus catálogos (`database.sql`), en una instalación de prueba nueva.
2. El DML de proveedores de Oscar (`dml_suppliers_logistics.sql`).
3. Este `dml_inventory.sql`.
4. El DML de ventas de Thomas, utilizando los IDs de lote de este archivo.

Seleccionar la base antes de cargar los DML. El archivo de Junior no incluye
CREATE, USE, UPDATE, DELETE ni instrucciones para desactivar claves foráneas.
Cargarlo una sola vez con estos rangos libres: repetirlo provoca un error de
clave duplicada; no sobrescribe ni ignora registros existentes.

## IDs y cantidades

| Tabla | Registros | IDs |
| --- | ---: | --- |
| productos | 50 | 1001–1050 |
| productos_proveedor | 50 | Parejas con proveedores 1, 2, 3, 4, 5, 6, 8 y 15 |
| lotes_inventario | 80 | 1001–1080 |
| registro_cuarentena | 50 | 1001–1050 |
| inspecciones_calidad | 75 | 1001–1075 |
| registro_mermas | 50 | 1001–1050 |
| operaciones_merma | 50 | SEED-JUNIOR-MERMA-1001 a SEED-JUNIOR-MERMA-1050 |

Las cinco tablas solicitadas tienen al menos 50 registros. Se incluyen además
`productos_proveedor`, necesario para registrar nuevos lotes con esos productos,
y `operaciones_merma`, necesario para que las bajas de prueba tengan la misma
protección ante reintentos que las registradas por el programa. Ningún proveedor
ni dato de ventas se inserta desde el archivo de Junior.

## Dependencias y coherencia

- Los proveedores referenciados están activos en el DML revisado de Oscar. Para
  enlatados se usa el proveedor 15, ya que el proveedor 7 está inactivo.
- Se utilizan los IDs de los catálogos del DDL base: categorías 1–8; estados de
  lote 1 Disponible, 2 Proximo a Vencer, 3 Vencido y 4 En Cuarentena. Si el equipo
  cambia esos IDs al consolidar, debe actualizar las referencias antes de cargar.
- Las fechas se calculan respecto al día de carga. Rechazo, merma y aprobación
  respetan el orden cronológico; los vencimientos permiten probar los estados.
- Hay 25 cuarentenas resueltas y 25 pendientes. Resolver la cuarentena no habilita
  un lote que ya está vencido. El lote 1030 tiene pérdida total y stock cero.
- Para cada lote, cantidad inicial menos sus mermas es igual a cantidad actual.
  Los datos aún no descuentan ventas de Thomas: al agregarlas, el equipo debe
  ajustar las existencias según las cantidades efectivamente vendidas.
- Ejemplos para ventas de prueba: lote 1004 disponible y lote 1001 próximo a
  vencer, ambos con stock. Los lotes 1002 (vencido) y 1026 (cuarentena) deben
  bloquearse. El lote 1030 permite probar falta de existencias.

## Verificación

La prueba `InventoryDmlMySqlTest` valida la estructura de INSERT y, al habilitar
MySQL, carga primero el DML real de Oscar y después este archivo en un esquema
aislado. Comprueba conteos, relaciones, saldos, fechas, estados y bloqueos de venta.
La prueba de carga requiere `ERP_TEST_SUPPLIERS_FILE` con la ruta al archivo de
Oscar, además de la configuración de pruebas MySQL descrita en el README.
