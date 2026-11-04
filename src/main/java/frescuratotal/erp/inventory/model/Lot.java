package frescuratotal.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lot {

    private final int id;
    private final String code;
    private final int productId;
    private final int supplierId;
    private final LocalDateTime receivedAt;
    private final LocalDate expirationDate;
    private final BigDecimal initialQuantity;
    private BigDecimal quantity;
    private final BigDecimal purchasePrice;
    private final BigDecimal salePrice;
    private final String aisle;
    private final String shelf;
    private int statusId;

    public Lot(int id, String code, int productId, int supplierId,
               LocalDateTime receivedAt, LocalDate expirationDate,
               BigDecimal initialQuantity, BigDecimal quantity,
               BigDecimal purchasePrice, BigDecimal salePrice,
               String aisle, String shelf, int statusId) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID del lote no puede ser negativo.");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código del lote es obligatorio.");
        }
        if (productId <= 0 || supplierId <= 0 || statusId <= 0) {
            throw new IllegalArgumentException("Los ID de producto, proveedor y estado deben ser positivos.");
        }
        if (receivedAt == null || expirationDate == null) {
            throw new IllegalArgumentException("Las fechas de ingreso y vencimiento son obligatorias.");
        }
        if (initialQuantity == null || initialQuantity.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad inicial debe ser mayor que cero.");
        }
        if (quantity == null || quantity.signum() < 0
                || quantity.compareTo(initialQuantity) > 0) {
            throw new IllegalArgumentException("La cantidad actual debe estar entre cero y la cantidad inicial.");
        }
        if (purchasePrice == null || purchasePrice.signum() < 0
                || salePrice == null || salePrice.signum() < 0) {
            throw new IllegalArgumentException("Los precios no pueden ser negativos.");
        }

        this.id = id;
        this.code = code.trim();
        this.productId = productId;
        this.supplierId = supplierId;
        this.receivedAt = receivedAt;
        this.expirationDate = expirationDate;
        this.initialQuantity = initialQuantity;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.aisle = aisle;
        this.shelf = shelf;
        this.statusId = statusId;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public int getProductId() {
        return productId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public BigDecimal getInitialQuantity() {
        return initialQuantity;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public String getAisle() {
        return aisle;
    }

    public String getShelf() {
        return shelf;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() < 0
                || quantity.compareTo(initialQuantity) > 0) {
            throw new IllegalArgumentException("La cantidad debe estar entre cero y la cantidad inicial.");
        }
        this.quantity = quantity;
    }

    public void setStatusId(int statusId) {
        if (statusId <= 0) {
            throw new IllegalArgumentException("El ID del estado debe ser positivo.");
        }
        this.statusId = statusId;
    }
}
