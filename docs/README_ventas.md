# Módulo de ventas y facturación

Paquete `frescuratotal.erp.sales`. Se accede desde el menú principal con la opción **4. Ventas y facturación**.

## Qué hace

- Confirma ventas de productos con existencias vendibles, a nombre de un cliente y de un cajero.
- Gestiona clientes (registrar, listar, actualizar y eliminar). Los tipos son Mayorista y Minorista.
- Genera la factura de cada venta: número de factura, CUFE simulado, PDF y código QR.

## Reglas de negocio

- Una venta solo puede usar lotes con existencias positivas, fecha de vencimiento futura y estado de inventario vendible.
- Los lotes se asignan por FEFO: primero el que vence antes y, si empatan, el de menor ID.
- Un lote que vence en los próximos siete días recibe un 50 % de descuento. Los lotes que vencen hoy se excluyen.
- Para este proyecto el equipo eligió un IVA único del 19 % en todas las líneas. La tasa se guarda en `detalles_venta.tasa_impuesto`.
- La base del impuesto de cada línea es el valor bruto menos su descuento. Los valores se redondean a dos decimales.
- El encabezado de la venta, el detalle, la factura y el descuento de existencias se confirman en una sola transacción JDBC. Si algo falla se hace `rollback`.
- El CUFE es simulado: SHA-256 de `numeroFactura|fechaHoraISO|totalConDosDecimales|NIT`. La fecha se comparte entre venta, factura y PDF. El QR apunta a una URL simulada y no se conecta con la DIAN. La identidad empresarial se consulta en `informacion_empresa`; no está escrita de forma fija en el generador.
- Después de facturar, Logística → opción 9 permite pasar una venta Registrada a En preparación. Repetir la transición, o aplicarla desde otro estado, se rechaza.

## Estructura

```
sales/
├── SalesModuleFactory       // Ensambla el módulo
├── controller/              // SalesController, CustomerController
├── model/                   // Customer, CustomerType, SaleItem, SaleReceipt, AvailableProduct, InvoiceLine, CompanyInfo
├── repository/              // Interfaces y clases JDBC, y LotSaleValidator
├── service/                 // SalesService, CustomerService, SaleAllocationPolicy, InvoiceFileService
└── view/                    // SalesView, CustomerView
```

## Coordinación con inventario

Ventas no repite las reglas de calidad de inventario. `SalesRepositoryImpl` recibe un `LotSaleValidator` (interfaz del paquete de ventas). La clase `InventoryLotSaleValidator`, en `inventory/model/dao`, lo implementa y delega en `InventoryQualityDAO.assertSellable`. Recibe la misma conexión (sin auto-commit) de la transacción de la venta, de modo que la validación y el descuento de existencias ocurren juntos. El ensamblado se hace en `Main`:

```java
SalesController salesController = SalesModuleFactory.create(scanner, new InventoryLotSaleValidator());
```

Se usa el mismo `Scanner` del menú principal para que no compitan dos lectores sobre `System.in`.

## Archivos generados y registros

- PDF y QR de cada factura: `output/invoices/`
- Registro de la aplicación: `logs/frescura-total.log`

Son archivos locales y están excluidos de Git. El PDF se abre con cualquier lector; el QR (`-qr.png`) con Fotos o con la cámara del celular.

## Datos de prueba

Los datos de ventas y clientes (50 clientes, 50 ventas con sus detalles y facturas) están incluidos en `docs/database.sql`. Los archivos `dml_customers.sql` y `dml_sales.sql` son los mismos datos por separado. No ejecute ambos sobre la misma base de datos: el consolidado ya los contiene.

## Pruebas

Las pruebas automáticas se ejecutan con **Clean and Build** en NetBeans o con `mvn test`. Cubren el orden FEFO, la división de cantidades entre lotes, el límite de siete días del descuento, el IVA, los lotes excluidos, el stock insuficiente y la generación de PDF y QR. Una prueba de venta contra la base de datos requiere una base de pruebas aparte; no se debe probar sobre datos compartidos.

## Diagrama de clases

`diagrama_clases_ventas.dot` es el código fuente editable (Graphviz) y `diagrama_clases_ventas.svg` es la imagen.
