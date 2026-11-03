package frescuratotal.erp.inventory.controller;

import frescuratotal.erp.inventory.model.Lot;
import frescuratotal.erp.inventory.model.dao.InventoryLotDAO;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

public class LotController {

    private final InventoryLotDAO lotDAO;

    public LotController() {
        this.lotDAO = new InventoryLotDAO();
    }

    public Lot registerLot(int productId, int supplierId,
                           LocalDate expirationDate, BigDecimal quantity,
                           BigDecimal purchasePrice, BigDecimal salePrice,
                           String aisle, String shelf) throws SQLException {
        if (productId <= 0 || supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Los ID del producto y proveedor deben ser mayores que cero.");
        }
        if (expirationDate == null || !expirationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de vencimiento debe ser posterior a hoy.");
        }
        válidateAmount(quantity, true, "cantidad");
        válidateAmount(purchasePrice, false, "precio de compra");
        válidateAmount(salePrice, false, "precio de venta");

        return lotDAO.save(productId, supplierId, expirationDate, quantity,
                purchasePrice, salePrice, optionalText(aisle), optionalText(shelf));
    }

    private void válidateAmount(BigDecimal amount, boolean positive, String field) {
        if (amount == null || amount.scale() > 2
                || amount.precision() - amount.scale() > 8
                || amount.signum() < 0 || (positive && amount.signum() == 0)) {
            throw new IllegalArgumentException(
                    ("cantidad".equals(field) ? "La " : "El ") + field
                    + " debe ser un número válido con máximo dos decimales.");
        }
    }

    private String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > 50) {
            throw new IllegalArgumentException("La ubicación no puede superar 50 caracteres.");
        }
        return text;
    }
}
