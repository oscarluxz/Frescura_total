# Sales and billing module

## Business rules

- A sale can use only lots with positive stock, a future expiration date, and a sellable inventory status.
- Lots are allocated by FEFO: earliest expiration date first, then lot ID as a stable tie-breaker.
- A lot expiring within the next seven days receives a 50% discount. Today-expiring lots are excluded.
- For this project, the team chose one IVA rate of 19% for every sale line. The schema stores that rate in `detalles_venta.tasa_impuesto`.
- The line tax base is the line gross amount less its discount. Money is rounded to two decimals with `HALF_UP`.
- Sale header, detail rows, invoice data, and stock deductions are committed in one JDBC transaction. A failure rolls the transaction back.
- CUFE is simulated as a SHA-256 hash of the sale and invoice identifiers and totals. The QR contains a local verification URL and does not connect to DIAN.

## Inventory coordination

`SalesRepositoryImpl` accepts a `LotSaleValidator`. During integration, wire the existing inventory rule rather than duplicating it:

`InventoryQualityDAO.assertSellable` receives the same non-auto-commit connection used by the sale transaction. Sales performs only the quantity subtraction and sale/invoice inserts.

The integrated inventory code is in package `frescuratotal.erp.inventory.model.dao`; this sales branch keeps its existing `com.frescuratotal.erp.sales` package. Integration still needs to connect the modules in the final `Main` and confirm the database connection configuration after the feature is merged.

## Application menu integration

When applying this module to the integrated `Main`, add a `4. Sales and billing` menu entry and route option `4` to:

```java
InventoryQualityDAO qualityDao = new InventoryQualityDAO();
SalesModuleFactory.create(scanner, qualityDao::assertSellable).start();
```

Pass the same `Scanner` already used by the main menu to avoid competing readers on `System.in`.

## Generated files and logs

- Invoice PDF and QR image: `output/invoices/`
- Application log: `logs/frescura-total.log`

These generated files are local artifacts and are excluded from Git.

Open the `.pdf` invoice with a PDF reader (for example Edge). Open the `-qr.png` image with Photos or scan it using a phone camera. The QR points to a simulated local verification URL; it is not connected to DIAN.

## Database seed data

Load the seed scripts in this order, once, in the same database:

1. `database.sql` and catalog data.
2. Oscar's `dml_suppliers_logistics.sql`.
3. Junior's `dml_inventory.sql` (provided separately by Junior).
4. `dml_customers.sql` (50 demo customers).
5. `dml_sales.sql` (50 sales, 50 details, and 50 invoices referencing Junior's lot IDs; it also subtracts those sold units from stock).

Junior's file contains 50 products, 50 product-supplier links, 80 lots, 50 quarantine records, 75 inspections, 50 waste records, and 50 waste-operation keys. It reserves explicit IDs and says to load it once. Coordinate with Oscar before consolidation so the files are not loaded twice or with occupied IDs.

The sales deliverables in this folder are the README, customer and sales seed scripts, and the class diagram source and image. They do not replace the team's consolidated database script.

## Tests

Run the automated Java tests from the project folder with `mvn test`. Five tests check FEFO ordering, quantity splitting, the seven-day discount boundary, IVA, excluded lots, insufficient stock, and that the QR and PDF files can be generated and read. A database-backed sale test still needs a separate test database with the integrated inventory code; do not run sales against production or shared data just to test.

## Class diagram

`diagrama_clases_ventas.dot` is the editable Graphviz source. `diagrama_clases_ventas.svg` is the image version; open it in a browser or insert it into the report.

## Integration status

This feature branch has not been merged into `develop`. Its `Main.java` is not the integrated team entry point, so adding the sales option must happen during final integration on the current team `Main`, passing its existing `Scanner` and `InventoryQualityDAO` validator.
