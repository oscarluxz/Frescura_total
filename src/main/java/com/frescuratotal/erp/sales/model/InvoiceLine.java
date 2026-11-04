package com.frescuratotal.erp.sales.model;

import java.math.BigDecimal;

public record InvoiceLine(String productName, int lotId, BigDecimal quantity,
                          BigDecimal unitPrice, BigDecimal discountRate,
                          BigDecimal discount, BigDecimal taxRate, BigDecimal tax,
                          BigDecimal subtotal, BigDecimal total) {
}
