package frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

public record SaleReceipt(int saleId, String saleNumber, String invoiceNumber, String cufe,
                          String customerName, String customerDocument, List<InvoiceLine> lines,
                          BigDecimal subtotal, BigDecimal discount, BigDecimal tax,
                          BigDecimal total, Path pdfPath, Path qrPath) {
}
