package frescuratotal.erp;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.inventory.controller.LotController;
import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.dao.*;
import frescuratotal.erp.sales.model.*;
import frescuratotal.erp.sales.repository.*;
import frescuratotal.erp.sales.service.*;
import frescuratotal.erp.suppliers.model.Supplier;
import frescuratotal.erp.suppliers.controller.SupplierController;
import frescuratotal.erp.suppliers.repository.*;
import frescuratotal.erp.logistics.controller.LogisticsController;
import frescuratotal.erp.logistics.model.*;
import frescuratotal.erp.logistics.repository.*;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

public class IntegratedWorkflowProbe {
    interface Probe { void run() throws Exception; }
    static int passed, failed, supplierId, vehicleId, driverId, routeId;
    static SaleReceipt receipt;
    static SalesService sales = new SalesService(new SalesRepositoryImpl(new InventoryLotSaleValidator(), new InvoiceFileService()));
    static SupplierController suppliers = new SupplierController(new JdbcSupplierRepository(),new JdbcPurchaseHistoryRepository());
    static LogisticsController logistics = new LogisticsController(new JdbcVehicleRepository(),new JdbcDriverRepository(),new JdbcDispatchOrderRepository(),new JdbcDeliveryRouteRepository());

    public static void main(String[] args) throws Exception {
        try { run(args); }
        finally { DatabaseConnection.getInstance().shutdown(); }
    }

    private static void run(String[] args) throws Exception {
        try (Connection c = open()) {
            if (!c.getCatalog().startsWith("erp_inventory_test_audit_")) throw new IllegalStateException("Test database required");
            try (Statement s = c.createStatement()) {
                for (String sql : statements(Files.readString(Path.of(args[0])))) {
                    if (sql.startsWith("CREATE DATABASE") || sql.startsWith("USE ")) continue;
                    s.execute(sql);
                }
            }
        }
        System.out.println("SQL_LOAD=PASS");
        try (Connection c=open(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SHOW TABLES")) {
            List<String> tables=new ArrayList<>(); while(r.next())tables.add(r.getString(1));
            for(String table:tables)System.out.println("ROWS "+table+"="+number("SELECT COUNT(*) FROM "+table));
        }
        check("Seed stock equals receipts minus losses and sales", () -> require(number(
                "SELECT COUNT(*) FROM lotes_inventario l LEFT JOIN (SELECT id_lote,SUM(cantidad_perdida) q FROM registro_mermas GROUP BY id_lote) m ON m.id_lote=l.id_lote "
                + "LEFT JOIN (SELECT id_lote,SUM(cantidad_vendida) q FROM detalles_venta GROUP BY id_lote) d ON d.id_lote=l.id_lote "
                + "WHERE l.cantidad<>l.cantidad_inicial-COALESCE(m.q,0)-COALESCE(d.q,0) OR l.cantidad<0")==0,"Inconsistent stock"));
        check("Supplier CRUD and catalog", () -> {
            Supplier supplier = new Supplier("Proveedor auditoría", "990001234-5", "Prueba", "audit@example.invalid", "3000000000", "Dirección de prueba");
            supplierId = suppliers.registerSupplier(supplier); supplier.setId(supplierId);
            require(suppliers.getSupplier(supplierId).getBusinessName().equals("Proveedor auditoría"),"Supplier not saved");
            supplier.setBusinessName("Proveedor auditoría actualizado"); suppliers.updateSupplier(supplier);
            suppliers.deactivateSupplier(supplierId); require(!suppliers.getSupplier(supplierId).isActive(),"Supplier not deactivated");
            suppliers.activateSupplier(supplierId);
            sql("INSERT INTO productos(id_producto,nombre,id_categoria,unidad_medida,precio_base) VALUES(900001,'Producto FEFO auditoría',1,'unidad',100),(900002,'Producto bloqueado auditoría',1,'unidad',100)");
            suppliers.addProductToCatalog(supplierId,900001); suppliers.addProductToCatalog(supplierId,900002);
            require(new JdbcSupplierRepository().findCatalog(supplierId).size()==2,"Catalog missing");
            suppliers.removeProductFromCatalog(supplierId,900002); suppliers.addProductToCatalog(supplierId,900002);
        });
        check("Synthetic supplier marker remains editable only for its fixture", () -> {
            Supplier fixture = suppliers.getSupplier(33);
            require("SYN-FT-900427-AG".equals(fixture.getNit()),"Missing assignment marker");
            suppliers.updateSupplier(fixture);
            fixture.setId(34);
            boolean rejected = false;
            try { suppliers.updateSupplier(fixture); } catch (IllegalArgumentException expected) { rejected = true; }
            require(rejected,"Synthetic marker accepted for another supplier");
        });
        check("Customer CRUD", () -> {
            CustomerRepositoryImpl repository=new CustomerRepositoryImpl();
            Customer customer=new Customer(0,"Cliente auditoría CRUD","AUDIT-CRUD","audit@example.invalid","3000000000","Dirección de prueba",CustomerType.WHOLESALE);
            repository.save(customer); int id=number("SELECT id_cliente FROM clientes WHERE documento='AUDIT-CRUD'");
            customer.setId(id); customer.setName("Cliente actualizado"); repository.update(customer);
            require(repository.findById(id).getName().equals("Cliente actualizado"),"Customer not updated");
            repository.delete(id); require(repository.findById(id)==null,"Customer not deleted");
        });
        check("Inventory registration and quality fixtures", () -> {
            sql("INSERT INTO clientes(id_cliente,nombre,documento,direccion,id_tipo) VALUES(900001,'Cliente auditoría 1','AUDIT-1','Dirección 1',1),(900002,'Cliente auditoría 2','AUDIT-2','Dirección 2',2)");
            addLot(900001,900001,3,3,2); addLot(900002,900001,15,10,1);
            addLot(900003,900001,-1,50,3); addLot(900004,900001,2,50,4); addLot(900005,900002,2,50,4);
            sql("INSERT INTO registro_cuarentena(id_lote,motivo,usuario) VALUES(900004,'Prueba','Auditoría'),(900005,'Prueba','Auditoría')");
            Lot lot = new LotController().registerLot(900001,supplierId,LocalDate.now().plusDays(30),new BigDecimal("10"),new BigDecimal("70"),new BigDecimal("100"),"AUDIT","1");
            require(lot.getCode().matches("LOT-[0-9]{8}-[0-9]{4,}"),"Wrong lot code");
            require(!suppliers.getPurchaseHistory(supplierId).isEmpty(),"Purchase history missing");
        });
        check("FEFO discount tax invoice PDF and QR", () -> {
            receipt=sales.confirmSale(900001,List.of(new SaleItem(900001,new BigDecimal("4"))),"Auditoría");
            require(receipt.lines().size()==2,"FEFO must split the order");
            require(receipt.lines().get(0).lotId()==900001 && receipt.lines().get(1).lotId()==900002,"Wrong FEFO order");
            require(receipt.subtotal().compareTo(new BigDecimal("400"))==0,"Wrong gross");
            require(receipt.discount().compareTo(new BigDecimal("150"))==0,"Wrong 50% discount");
            require(receipt.tax().compareTo(new BigDecimal("47.50"))==0,"Wrong 19% tax");
            require(receipt.total().compareTo(new BigDecimal("297.50"))==0,"Wrong total");
            require(number("SELECT cantidad FROM lotes_inventario WHERE id_lote=900001")==0,"Near-expiry lot not consumed");
            require(number("SELECT cantidad FROM lotes_inventario WHERE id_lote=900002")==9,"Available lot quantity wrong");
            require(Files.size(receipt.pdfPath())>0 && Files.size(receipt.qrPath())>0,"Invoice files missing");
            var image=ImageIO.read(receipt.qrPath().toFile());
            String qr=new MultiFormatReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText();
            require(qr.equals("https://frescuratotal.local/verify?cufe="+receipt.cufe()),"QR and CUFE mismatch");
            String payload = String.join("|", receipt.invoiceNumber(), receipt.getIssuedAt().toString(),
                    receipt.total().toPlainString(), receipt.getCompany().getNit());
            String expectedHash = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            require(expectedHash.equals(receipt.cufe()), "CUFE does not include the invoice identity, date, amount and NIT");
            try (Connection connection = open(); PreparedStatement statement = connection.prepareStatement(
                    "SELECT v.fecha_venta,f.fecha_emision FROM ventas v JOIN facturas f ON f.id_venta=v.id_venta WHERE v.id_venta=?")) {
                statement.setInt(1, receipt.saleId());
                try (ResultSet result = statement.executeQuery()) {
                    require(result.next(), "Persisted invoice missing");
                    require(result.getTimestamp(1).toLocalDateTime().equals(receipt.getIssuedAt()), "Sale date mismatch");
                    require(result.getTimestamp(2).toLocalDateTime().equals(receipt.getIssuedAt()), "Invoice date mismatch");
                }
            }
            System.out.println("INVOICE="+receipt.pdfPath().toAbsolutePath());
            System.out.println("QR="+receipt.qrPath().toAbsolutePath());
        });
        check("Blocked lots cannot be sold", () -> {
            int count=number("SELECT COUNT(*) FROM ventas"); boolean rejected=false;
            try { sales.confirmSale(900001,List.of(new SaleItem(900002,BigDecimal.ONE)),"Auditoría"); }
            catch(IllegalArgumentException expected){rejected=true;}
            require(rejected && number("SELECT COUNT(*) FROM ventas")==count,"Blocked sale persisted");
        });
        check("Invoice failure rolls back sale and inventory", () -> {
            int count=number("SELECT COUNT(*) FROM ventas"), stock=number("SELECT SUM(cantidad) FROM lotes_inventario WHERE id_producto=900001");
            SalesService failing=new SalesService(new SalesRepositoryImpl(new InventoryLotSaleValidator(),new InvoiceFileService(){
                @Override public SaleReceipt createFiles(SaleReceipt value){throw new IllegalStateException("Forced audit invoice failure");}
            })); boolean rejected=false;
            try { failing.confirmSale(900001,List.of(new SaleItem(900001,BigDecimal.ONE)),"Auditoría"); }
            catch(IllegalStateException expected){rejected=true;}
            require(rejected && number("SELECT COUNT(*) FROM ventas")==count,"Sale not rolled back");
            require(number("SELECT SUM(cantidad) FROM lotes_inventario WHERE id_producto=900001")==stock,"Stock not rolled back");
        });
        check("Concurrent sales do not oversell", () -> {
            ExecutorService pool=Executors.newFixedThreadPool(2); CountDownLatch gate=new CountDownLatch(1);
            try {
                Future<Boolean> a=pool.submit(()->concurrentSale(gate,900001));
                Future<Boolean> b=pool.submit(()->concurrentSale(gate,900002)); gate.countDown();
                require(a.get(25,TimeUnit.SECONDS)!=b.get(25,TimeUnit.SECONDS),"Exactly one concurrent sale must succeed");
                require(number("SELECT COUNT(*) FROM lotes_inventario WHERE cantidad<0")==0,"Negative stock");
            } finally {pool.shutdownNow();}
        });
        check("New confirmed sale can enter dispatch without manual SQL", () -> {
            require(receipt!=null,"No sale receipt");
            logistics.prepareSale(receipt.saleId());
            boolean duplicateRejected = false;
            try { logistics.prepareSale(receipt.saleId()); } catch (SQLException expected) { duplicateRejected = true; }
            require(duplicateRejected, "An already prepared sale accepted another preparation");
            require(logistics.getDispatchPanel().stream().anyMatch(order->order.getSaleId()==receipt.saleId()),
                    "Sale remains Registrada and panel only accepts En Preparacion; no application transition found");
        });
        check("Route creation transit delivery and traceability", () -> {
            require(receipt!=null,"No sale receipt");
            vehicleId=logistics.registerVehicle(new Vehicle("AUD741","Vehículo auditoría",1000));
            driverId=logistics.registerDriver(new frescuratotal.erp.logistics.model.Driver("Conductor auditoría","AUDIT-DRIVER-1","3000000000","driver@example.invalid"));
            Timestamp departure=Timestamp.valueOf(java.time.LocalDateTime.now().plusHours(1));
            Timestamp arrival=Timestamp.valueOf(java.time.LocalDateTime.now().plusHours(2));
            routeId=logistics.createRoute("Ruta auditoría",vehicleId,driverId,List.of(receipt.saleId()),"Auditoría",departure,arrival);
            boolean rejected=false;
            try {logistics.confirmDelivery(routeId,receipt.saleId(),"Receptor","Prueba");}catch(IllegalStateException expected){rejected=true;}
            require(rejected,"Delivery before departure was allowed");
            logistics.startRoute(routeId); logistics.confirmDelivery(routeId,receipt.saleId(),"Receptor","Prueba");
            require(logistics.getRoute(routeId).getStatus()==RouteStatus.COMPLETED,"Route not completed");
            require(new TraceabilityDAO().traceLot(900001).getCustomers().stream().anyMatch(c->c.getId()==900001),"Delivered customer missing from traceability");
        });
        System.out.println("AUDIT_SUMMARY passed="+passed+" failed="+failed);
        if (failed > 0) { throw new AssertionError("Integrated workflow failures: " + failed); }
    }
    static boolean concurrentSale(CountDownLatch gate,int customer) throws Exception {
        gate.await(); try {sales.confirmSale(customer,List.of(new SaleItem(900001,new BigDecimal("15"))),"Auditoría");return true;}
        catch(IllegalArgumentException expected){return false;}
    }
    static Connection open()throws SQLException{return DatabaseConnection.getInstance().getConnection();}
    static void sql(String sql)throws SQLException{try(Connection c=open();Statement s=c.createStatement()){s.executeUpdate(sql);}}
    static int number(String sql)throws SQLException{try(Connection c=open();Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    static void addLot(int id,int product,int days,int quantity,int state)throws SQLException{
        try(Connection c=open();PreparedStatement s=c.prepareStatement("INSERT INTO lotes_inventario(id_lote,numero_lote,id_producto,id_proveedor,fecha_vencimiento,cantidad_inicial,cantidad,precio_compra,precio_venta,id_estado) VALUES(?,?,?,?,?,?,?,?,?,?)")){
            s.setInt(1,id);s.setString(2,"AUDIT-"+id);s.setInt(3,product);s.setInt(4,supplierId);s.setDate(5,java.sql.Date.valueOf(LocalDate.now().plusDays(days)));s.setInt(6,quantity);s.setInt(7,quantity);s.setBigDecimal(8,new BigDecimal("70"));s.setBigDecimal(9,new BigDecimal("100"));s.setInt(10,state);s.executeUpdate();
        }
    }
    static void check(String label,Probe probe){try{probe.run();passed++;System.out.println("PASS | "+label);}catch(Throwable error){failed++;System.out.println("FAIL | "+label+" | "+error);error.printStackTrace(System.out);}}
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    static List<String> statements(String source) {
        return frescuratotal.erp.support.SqlTestDatabase.statements(source);
    }
}
