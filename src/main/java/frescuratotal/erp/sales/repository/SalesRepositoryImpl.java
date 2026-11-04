package frescuratotal.erp.sales.repository;

import frescuratotal.erp.common.DatabaseConnection;
import frescuratotal.erp.sales.model.AvailableProduct;
import frescuratotal.erp.sales.model.InvoiceLine;
import frescuratotal.erp.sales.model.SaleItem;
import frescuratotal.erp.sales.model.SaleReceipt;
import frescuratotal.erp.sales.service.InvoiceFileService;
import frescuratotal.erp.sales.service.SaleAllocationPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalesRepositoryImpl implements SalesRepository {
    private static final Logger LOGGER = Logger.getLogger(SalesRepositoryImpl.class.getName());
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Bogota");

    private final LotSaleValidator lotSaleValidator;
    private final InvoiceFileService invoiceFileService;

    public SalesRepositoryImpl(LotSaleValidator lotSaleValidator, InvoiceFileService invoiceFileService) {
        if (lotSaleValidator == null || invoiceFileService == null) {
            throw new IllegalArgumentException("Se requieren el validador de lotes y el servicio de archivos de factura.");
        }
        this.lotSaleValidator = lotSaleValidator;
        this.invoiceFileService = invoiceFileService;
    }

    @Override
    public List<AvailableProduct> findAvailableProducts() {
        String sql = "SELECT p.id_producto, p.nombre, p.unidad_medida, "
                + "SUM(l.cantidad) AS cantidad_disponible, MIN(l.precio_venta) AS precio_desde "
                + "FROM productos p JOIN lotes_inventario l ON l.id_producto = p.id_producto "
                + "JOIN estados_lote e ON e.id_estado = l.id_estado "
                + "WHERE l.cantidad > 0 AND l.fecha_vencimiento > CURDATE() "
                + "AND e.nombre IN ('Disponible', 'Proximo a Vencer') "
                + "GROUP BY p.id_producto, p.nombre, p.unidad_medida ORDER BY p.nombre";
        List<AvailableProduct> products = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                products.add(new AvailableProduct(
                        result.getInt("id_producto"), result.getString("nombre"),
                        result.getString("unidad_medida"), result.getBigDecimal("cantidad_disponible"),
                        result.getBigDecimal("precio_desde")));
            }
            return products;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar los productos disponibles para la venta.", e);
        }
    }

    @Override
    public SaleReceipt registerSale(int customerId, List<SaleItem> items, String cashier) {
        SaleReceipt completeReceipt = null;
        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                CustomerRow customer = requireCustomer(connection, customerId);
                List<Allocation> allocations = allocateByFefo(connection, items);
                Totals totals = calculateTotals(allocations);
                String saleNumber = "SALE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                int registeredStatusId = findRegisteredStatusId(connection);
                int saleId = insertSale(connection, saleNumber, customerId, cashier,
                        registeredStatusId, totals);

                for (Allocation allocation : allocations) {
                    lotSaleValidator.assertSellable(connection, allocation.lotId(), allocation.quantity());
                    deductLot(connection, allocation.lotId(), allocation.quantity());
                    insertSaleDetail(connection, saleId, allocation);
                }

                String invoiceNumber = "FT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                String cufe = createCufe(saleId, saleNumber, invoiceNumber, customerId, totals);
                insertInvoice(connection, saleId, invoiceNumber, cufe, totals);
                List<InvoiceLine> invoiceLines = allocations.stream()
                        .map(line -> new InvoiceLine(line.productName(), line.lotId(), line.quantity(),
                                line.unitPrice(), line.discountRate(), line.discount(), line.taxRate(),
                                line.tax(), line.gross(), line.lineTotal()))
                        .toList();
                SaleReceipt receipt = new SaleReceipt(saleId, saleNumber, invoiceNumber, cufe,
                        customer.name(), customer.document(), invoiceLines,
                        totals.subtotal(), totals.discount(), totals.tax(), totals.total(), null, null);
                completeReceipt = invoiceFileService.createFiles(receipt);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                rollback(connection, e);
                if (completeReceipt != null) {
                    try {
                        invoiceFileService.deleteFiles(completeReceipt);
                    } catch (RuntimeException cleanupError) {
                        e.addSuppressed(cleanupError);
                    }
                }
                throw e;
            }
        } catch (SQLException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Falló el registro de la venta para el cliente ID " + customerId, e);
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalStateException("No se pudo registrar la venta: " + e.getMessage(), e);
        }

        LOGGER.info("Venta registrada " + completeReceipt.saleNumber()
                + " y factura " + completeReceipt.invoiceNumber());
        return completeReceipt;
    }

    private List<Allocation> allocateByFefo(Connection connection, List<SaleItem> items) throws SQLException {
        List<Allocation> allocations = new ArrayList<>();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        for (SaleItem item : items) {
            List<SaleAllocationPolicy.Lot> lots = lockSellableLots(connection, item.productId()).stream()
                    .map(lot -> new SaleAllocationPolicy.Lot(lot.id(), lot.productName(),
                            lot.expirationDate(), lot.quantity(), lot.salePrice(), true))
                    .toList();
            try {
                SaleAllocationPolicy.allocate(item.quantity(), lots, today).stream()
                        .map(line -> new Allocation(line.lotId(), line.productName(), line.quantity(),
                                line.unitPrice(), line.discountRate(), line.discount(), line.taxRate(),
                                line.tax(), line.gross(), line.lineTotal()))
                        .forEach(allocations::add);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Existencias vendibles insuficientes para el producto ID "
                        + item.productId() + ". " + e.getMessage(), e);
            }
        }
        return allocations;
    }

    private List<LotRow> lockSellableLots(Connection connection, int productId) throws SQLException {
        String sql = "SELECT l.id_lote, p.nombre AS producto, l.fecha_vencimiento, l.cantidad, l.precio_venta "
                + "FROM lotes_inventario l JOIN estados_lote e ON e.id_estado = l.id_estado "
                + "JOIN productos p ON p.id_producto = l.id_producto "
                + "WHERE l.id_producto = ? AND l.cantidad > 0 AND l.fecha_vencimiento > CURDATE() "
                + "AND e.nombre IN ('Disponible', 'Proximo a Vencer') "
                + "ORDER BY l.fecha_vencimiento ASC, l.id_lote ASC FOR UPDATE";
        List<LotRow> lots = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    lots.add(new LotRow(result.getInt("id_lote"), result.getString("producto"),
                            result.getDate("fecha_vencimiento").toLocalDate(),
                            result.getBigDecimal("cantidad"), result.getBigDecimal("precio_venta")));
                }
            }
        }
        return lots;
    }

    private CustomerRow requireCustomer(Connection connection, int customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT nombre, documento FROM clientes WHERE id_cliente = ? FOR UPDATE")) {
            statement.setInt(1, customerId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new IllegalArgumentException("Cliente no encontrado: " + customerId);
                }
                return new CustomerRow(result.getString("nombre"), result.getString("documento"));
            }
        }
    }

    private int findRegisteredStatusId(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id_estado FROM estados_venta WHERE nombre = 'Registrada'");
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new SQLException("Falta el estado de venta 'Registrada' en estados_venta.");
            }
            return result.getInt("id_estado");
        }
    }

    private int insertSale(Connection connection, String saleNumber, int customerId, String cashier,
                           int statusId, Totals totals) throws SQLException {
        String sql = "INSERT INTO ventas (numero_venta, id_cliente, subtotal, total_descuento, "
                + "total_impuesto, monto_total, id_estado, usuario_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, saleNumber);
            statement.setInt(2, customerId);
            statement.setBigDecimal(3, totals.subtotal());
            statement.setBigDecimal(4, totals.discount());
            statement.setBigDecimal(5, totals.tax());
            statement.setBigDecimal(6, totals.total());
            statement.setInt(7, statusId);
            statement.setString(8, cashier);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se insertó el encabezado de la venta.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No se pudo obtener el ID de la nueva venta.");
                }
                return keys.getInt(1);
            }
        }
    }

    private void deductLot(Connection connection, int lotId, BigDecimal quantity) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE lotes_inventario SET cantidad = cantidad - ? "
                        + "WHERE id_lote = ? AND cantidad >= ?")) {
            statement.setBigDecimal(1, quantity);
            statement.setInt(2, lotId);
            statement.setBigDecimal(3, quantity);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("El lote " + lotId + " ya no tiene existencias suficientes.");
            }
        }
    }

    private void insertSaleDetail(Connection connection, int saleId, Allocation allocation) throws SQLException {
        String sql = "INSERT INTO detalles_venta (id_venta, id_lote, cantidad_vendida, precio_unitario, "
                + "descuento_aplicado, monto_descuento, tasa_impuesto, monto_impuesto, subtotal, total_linea) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            statement.setInt(2, allocation.lotId());
            statement.setBigDecimal(3, allocation.quantity());
            statement.setBigDecimal(4, allocation.unitPrice());
            statement.setBigDecimal(5, allocation.discountRate());
            statement.setBigDecimal(6, allocation.discount());
            statement.setBigDecimal(7, allocation.taxRate());
            statement.setBigDecimal(8, allocation.tax());
            statement.setBigDecimal(9, allocation.gross());
            statement.setBigDecimal(10, allocation.lineTotal());
            statement.executeUpdate();
        }
    }

    private void insertInvoice(Connection connection, int saleId, String invoiceNumber,
                               String cufe, Totals totals) throws SQLException {
        String qrContent = "https://frescuratotal.local/verify?cufe=" + cufe;
        String sql = "INSERT INTO facturas (id_venta, numero_factura, cufe, enlace_qr, subtotal, "
                + "total_descuento, monto_impuesto, monto_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            statement.setString(2, invoiceNumber);
            statement.setString(3, cufe);
            statement.setString(4, qrContent);
            statement.setBigDecimal(5, totals.subtotal());
            statement.setBigDecimal(6, totals.discount());
            statement.setBigDecimal(7, totals.tax());
            statement.setBigDecimal(8, totals.total());
            statement.executeUpdate();
        }
    }

    private Totals calculateTotals(List<Allocation> allocations) {
        BigDecimal subtotal = ZERO;
        BigDecimal discount = ZERO;
        BigDecimal tax = ZERO;
        for (Allocation allocation : allocations) {
            subtotal = subtotal.add(allocation.gross());
            discount = discount.add(allocation.discount());
            tax = tax.add(allocation.tax());
        }
        subtotal = money(subtotal);
        discount = money(discount);
        tax = money(tax);
        return new Totals(subtotal, discount, tax, money(subtotal.subtract(discount).add(tax)));
    }

    private String createCufe(int saleId, String saleNumber, String invoiceNumber,
                              int customerId, Totals totals) {
        String payload = String.join("|", Integer.toString(saleId), saleNumber, invoiceNumber,
                Integer.toString(customerId), totals.subtotal().toPlainString(),
                totals.discount().toPlainString(), totals.tax().toPlainString(), totals.total().toPlainString());
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible.", e);
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private static final class CustomerRow {
        private final String name;
        private final String document;

        private CustomerRow(String name, String document) {
            this.name = name;
            this.document = document;
        }

        public String name() { return name; }
        public String getName() { return name; }
        public String document() { return document; }
        public String getDocument() { return document; }

        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof CustomerRow)) { return false; }
            CustomerRow value = (CustomerRow) other;
            return java.util.Objects.equals(name, value.name)
                    && java.util.Objects.equals(document, value.document);
        }
        @Override public int hashCode() { return java.util.Objects.hash(name, document); }
    }
    private static final class LotRow {
        private final int id;
        private final String productName;
        private final LocalDate expirationDate;
        private final BigDecimal quantity;
        private final BigDecimal salePrice;

        private LotRow(int id, String productName, LocalDate expirationDate, BigDecimal quantity, BigDecimal salePrice) {
            this.id = id;
            this.productName = productName;
            this.expirationDate = expirationDate;
            this.quantity = quantity;
            this.salePrice = salePrice;
        }

        public int id() { return id; }
        public int getId() { return id; }
        public String productName() { return productName; }
        public String getProductName() { return productName; }
        public LocalDate expirationDate() { return expirationDate; }
        public LocalDate getExpirationDate() { return expirationDate; }
        public BigDecimal quantity() { return quantity; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal salePrice() { return salePrice; }
        public BigDecimal getSalePrice() { return salePrice; }

        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof LotRow)) { return false; }
            LotRow value = (LotRow) other;
            return id == value.id
                    && java.util.Objects.equals(productName, value.productName)
                    && java.util.Objects.equals(expirationDate, value.expirationDate)
                    && java.util.Objects.equals(quantity, value.quantity)
                    && java.util.Objects.equals(salePrice, value.salePrice);
        }
        @Override public int hashCode() { return java.util.Objects.hash(id, productName, expirationDate, quantity, salePrice); }
    }
    private static final class Allocation {
        private final int lotId;
        private final String productName;
        private final BigDecimal quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal discountRate;
        private final BigDecimal discount;
        private final BigDecimal taxRate;
        private final BigDecimal tax;
        private final BigDecimal gross;
        private final BigDecimal lineTotal;

        private Allocation(int lotId, String productName, BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountRate, BigDecimal discount, BigDecimal taxRate, BigDecimal tax, BigDecimal gross, BigDecimal lineTotal) {
            this.lotId = lotId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountRate = discountRate;
            this.discount = discount;
            this.taxRate = taxRate;
            this.tax = tax;
            this.gross = gross;
            this.lineTotal = lineTotal;
        }

        public int lotId() { return lotId; }
        public int getLotId() { return lotId; }
        public String productName() { return productName; }
        public String getProductName() { return productName; }
        public BigDecimal quantity() { return quantity; }
        public BigDecimal getQuantity() { return quantity; }
        public BigDecimal unitPrice() { return unitPrice; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal discountRate() { return discountRate; }
        public BigDecimal getDiscountRate() { return discountRate; }
        public BigDecimal discount() { return discount; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal taxRate() { return taxRate; }
        public BigDecimal getTaxRate() { return taxRate; }
        public BigDecimal tax() { return tax; }
        public BigDecimal getTax() { return tax; }
        public BigDecimal gross() { return gross; }
        public BigDecimal getGross() { return gross; }
        public BigDecimal lineTotal() { return lineTotal; }
        public BigDecimal getLineTotal() { return lineTotal; }

        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof Allocation)) { return false; }
            Allocation value = (Allocation) other;
            return lotId == value.lotId
                    && java.util.Objects.equals(productName, value.productName)
                    && java.util.Objects.equals(quantity, value.quantity)
                    && java.util.Objects.equals(unitPrice, value.unitPrice)
                    && java.util.Objects.equals(discountRate, value.discountRate)
                    && java.util.Objects.equals(discount, value.discount)
                    && java.util.Objects.equals(taxRate, value.taxRate)
                    && java.util.Objects.equals(tax, value.tax)
                    && java.util.Objects.equals(gross, value.gross)
                    && java.util.Objects.equals(lineTotal, value.lineTotal);
        }
        @Override public int hashCode() { return java.util.Objects.hash(lotId, productName, quantity, unitPrice, discountRate, discount, taxRate, tax, gross, lineTotal); }
    }
    private static final class Totals {
        private final BigDecimal subtotal;
        private final BigDecimal discount;
        private final BigDecimal tax;
        private final BigDecimal total;

        private Totals(BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) {
            this.subtotal = subtotal;
            this.discount = discount;
            this.tax = tax;
            this.total = total;
        }

        public BigDecimal subtotal() { return subtotal; }
        public BigDecimal getSubtotal() { return subtotal; }
        public BigDecimal discount() { return discount; }
        public BigDecimal getDiscount() { return discount; }
        public BigDecimal tax() { return tax; }
        public BigDecimal getTax() { return tax; }
        public BigDecimal total() { return total; }
        public BigDecimal getTotal() { return total; }

        @Override public boolean equals(Object other) {
            if (this == other) { return true; }
            if (!(other instanceof Totals)) { return false; }
            Totals value = (Totals) other;
            return java.util.Objects.equals(subtotal, value.subtotal)
                    && java.util.Objects.equals(discount, value.discount)
                    && java.util.Objects.equals(tax, value.tax)
                    && java.util.Objects.equals(total, value.total);
        }
        @Override public int hashCode() { return java.util.Objects.hash(subtotal, discount, tax, total); }
    }
}
