package frescuratotal.erp.inventory.model;

import java.math.BigDecimal;

public class Product {

    private final int id;
    private final String name;
    private final String description;
    private final int categoryId;
    private final String unitOfMeasure;
    private final BigDecimal basePrice;

    public Product(int id, String name, String description, int categoryId,
                   String unitOfMeasure, BigDecimal basePrice) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID del producto no puede ser negativo.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (categoryId <= 0) {
            throw new IllegalArgumentException("El ID de la categoría debe ser positivo.");
        }
        if (unitOfMeasure == null || unitOfMeasure.isBlank()) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        }
        if (basePrice != null && basePrice.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description;
        this.categoryId = categoryId;
        this.unitOfMeasure = unitOfMeasure.trim();
        this.basePrice = basePrice;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }
}
