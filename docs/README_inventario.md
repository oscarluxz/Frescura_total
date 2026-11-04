# Inventario y calidad — ERP Frescura Total integrado

El módulo de Junior está integrado con ventas, proveedores y logística. Utiliza
Java 17, Maven, JDBC y POO clásica, sin records ni frameworks de persistencia.
La configuración general y el recorrido completo están en el README principal.

## Operaciones de consola

| Opción de inventario | Operación |
| --- | --- |
| 1 | Registrar lote con producto y proveedor asociados, cantidades, precios, fecha y ubicación |
| 2 | Actualizar vencimientos conservando cuarentenas |
| 3 | Consultar existencias, alertas y vencidos pendientes de baja |
| 4 | Registrar cuarentena con motivo y responsable |
| 5 | Aprobar o rechazar como Inspector |
| 6 | Registrar merma con clave de operación para reintentos |
| 7 | Rastrear proveedor original y clientes con entregas confirmadas |
| 8 / 9 | Consultar lotes por proveedor o cliente |
| 10 | Consultar historial de inspecciones |

Abra `frescuratotal.erp.Main`. Su opción 5 cambia el rol de demostración:
Operador (inicial) o Inspector. La selección es una simulación académica, sin
autenticación. El controlador de calidad verifica el rol antes de acceder a JDBC;
escribir un nombre de inspector en una sesión Operador no concede autorización.

Al entrar al menú principal se simula la revisión diaria. Se repite automáticamente
al cambiar la fecha de Bogotá mientras se navega por ese menú. La opción 2 de
inventario permite forzar otra revisión. No se instala una tarea ni servicio del sistema.

## Estados y calidad

- Vence hoy o antes: EXPIRED / Vencido, bloqueado para ventas. Si quedan existencias,
  la consulta lo marca como PENDIENTE DE BAJA. Se registran mediante la opción 6.
- Entre mañana y siete días: NEAR_EXPIRY / Proximo a Vencer, con alerta.
- Después de siete días: AVAILABLE / Disponible.
- La cuarentena prevalece sobre el calendario. Solo una inspección aprobada
  resuelve una cuarentena y calcula el estado por fecha; un vencido no se libera para ventas.
- Un rechazo nuevo registra la inspección, la merma de todo el saldo, su clave de
  operación, la cantidad cero y la resolución de la cuarentena en la misma
  transacción. El lote conserva En Cuarentena como bloqueo histórico, sin stock.
  No se permite aprobarlo después ni registrar otra baja de esas unidades.
- Las mermas manuales permiten pérdidas parciales y totales. Validan DECIMAL(10,2),
  saldo suficiente y responsable. Si hay fallo, se revierte tanto el stock como
  el historial.
- La misma clave y los mismos datos devuelven la merma anterior sin descontar otra
  vez. Otra cantidad, motivo o responsable con esa clave se rechaza. Dos claves
  diferentes representan operaciones diferentes.
- Se mantiene compatibilidad con inspecciones históricas rechazadas pendientes
  de baja: la inspección debe ser la última del mismo lote, con cuarentena abierta
  y sin otra merma asociada.
- La trazabilidad de clientes incluye únicamente ventas Entregada, a través de
  detalles_venta → ventas → clientes.

## Integración JDBC

`InventoryLotSaleValidator` adapta la interfaz `LotSaleValidator` de ventas.
Ventas le pasa su misma conexión, con auto-commit desactivado, antes de descontar
el lote. La validación bloquea la fila hasta commit/rollback, actualiza su estado
y rechaza cuarentenas, vencidos, cantidades inválidas y saldo insuficiente.
No descuenta stock por sí sola; lo hace la transacción de ventas junto al detalle,
factura y generación de archivos.

Los catálogos de proveedores/productos y el historial de ingresos usan las
relaciones del esquema oficial. El DDL original se contrastó con la descarga
oficial del enunciado. `docs/database.sql` contiene las 22 tablas de origen,
la extensión `operaciones_merma` y datos coherentes de los cuatro módulos.

Para una instalación nueva use una base vacía y el consolidado. Si la base ya
tiene el esquema original, la extensión de mermas está separada en
`docs/migrations/001_waste_operations.sql`; no vuelva a cargar todos los INSERT.
Las variables de conexión son `ERP_DB_PASSWORD`, `ERP_DB_USER`, `ERP_DB_HOST`,
`ERP_DB_PORT`, `ERP_DB_NAME` y, opcionalmente, `ERP_DB_URL`. No se carga
automáticamente un archivo .env.

## Verificación y diagramas

Las instrucciones reproducibles y el resultado actual están en
`docs/verificacion_integrada.md` y en el README principal. La suite comprueba
fechas límite, bloqueos, rechazo con baja atómica, rollback por fallo provocado,
idempotencia y concurrencia de mermas, inspecciones históricas, trazabilidad,
claves foráneas y carga del consolidado. El recorrido adicional comprueba FEFO,
factura/QR, concurrencia de ventas y venta → preparación → ruta → entrega.

`docs/diagrama_er.png` y su SVG muestran tablas y cardinalidades. El nuevo
`docs/diagrama_clases.png` incluye el inventario y los otros tres módulos.
Los generadores están en `docs/tools`, fuera de la ejecución de Java.
`docs/inventory-verification.md` se conserva como informe histórico del tercer
avance; sus cantidades de pruebas no representan la versión integrada actual.
