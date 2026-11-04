package frescuratotal.erp.sales.model;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

public final class SaleReceipt {
    private final int saleId;
    private final String saleNumber;
    private final String invoiceNumber;
    private final String cufe;
    private final String customerName;
    private final String customerDocument;
    private final List<InvoiceLine> lines;
    private final BigDecimal subtotal;
    private final BigDecimal discount;
    private final BigDecimal tax;
    private final BigDecimal total;
    private final Path pdfPath;
    private final Path qrPath;
    private final CompanyInfo company;
    private final java.time.LocalDateTime issuedAt;
    private final String customerAddress;

    public SaleReceipt(int saleId, String saleNumber, String invoiceNumber, String cufe, String customerName, String customerDocument, List<InvoiceLine> lines, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total, Path pdfPath, Path qrPath, CompanyInfo company, java.time.LocalDateTime issuedAt, String customerAddress) {
        this.saleId = saleId;
        this.saleNumber = saleNumber;
        this.invoiceNumber = invoiceNumber;
        this.cufe = cufe;
        this.customerName = customerName;
        this.customerDocument = customerDocument;
        this.lines = java.util.List.copyOf(lines);
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.total = total;
        this.pdfPath = pdfPath;
        this.qrPath = qrPath;
        this.company = java.util.Objects.requireNonNull(company);
        this.issuedAt = java.util.Objects.requireNonNull(issuedAt);
        this.customerAddress = customerAddress;
    }

    public int saleId() { return saleId; }
    public int getSaleId() { return saleId; }
    public String saleNumber() { return saleNumber; }
    public String getSaleNumber() { return saleNumber; }
    public String invoiceNumber() { return invoiceNumber; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String cufe() { return cufe; }
    public String getCufe() { return cufe; }
    public String customerName() { return customerName; }
    public String getCustomerName() { return customerName; }
    public String customerDocument() { return customerDocument; }
    public String getCustomerDocument() { return customerDocument; }
    public List<InvoiceLine> lines() { return lines; }
    public List<InvoiceLine> getLines() { return lines; }
    public BigDecimal subtotal() { return subtotal; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal discount() { return discount; }
    public BigDecimal getDiscount() { return discount; }
    public BigDecimal tax() { return tax; }
    public BigDecimal getTax() { return tax; }
    public BigDecimal total() { return total; }
    public BigDecimal getTotal() { return total; }
    public Path pdfPath() { return pdfPath; }
    public Path getPdfPath() { return pdfPath; }
    public Path qrPath() { return qrPath; }
    public Path getQrPath() { return qrPath; }
    public CompanyInfo getCompany() { return company; }
    public java.time.LocalDateTime getIssuedAt() { return issuedAt; }
    public String getCustomerAddress() { return customerAddress; }

    @Override public boolean equals(Object other) {
        if (this == other) { return true; }
        if (!(other instanceof SaleReceipt)) { return false; }
        SaleReceipt value = (SaleReceipt) other;
        return saleId == value.saleId
                && java.util.Objects.equals(saleNumber, value.saleNumber)
                && java.util.Objects.equals(invoiceNumber, value.invoiceNumber)
                && java.util.Objects.equals(cufe, value.cufe)
                && java.util.Objects.equals(customerName, value.customerName)
                && java.util.Objects.equals(customerDocument, value.customerDocument)
                && java.util.Objects.equals(lines, value.lines)
                && java.util.Objects.equals(subtotal, value.subtotal)
                && java.util.Objects.equals(discount, value.discount)
                && java.util.Objects.equals(tax, value.tax)
                && java.util.Objects.equals(total, value.total)
                && java.util.Objects.equals(pdfPath, value.pdfPath)
                && java.util.Objects.equals(qrPath, value.qrPath)
                && java.util.Objects.equals(company, value.company)
                && java.util.Objects.equals(issuedAt, value.issuedAt)
                && java.util.Objects.equals(customerAddress, value.customerAddress);
    }
    @Override public int hashCode() { return java.util.Objects.hash(saleId, saleNumber, invoiceNumber, cufe, customerName, customerDocument, lines, subtotal, discount, tax, total, pdfPath, qrPath, company, issuedAt, customerAddress); }
}
