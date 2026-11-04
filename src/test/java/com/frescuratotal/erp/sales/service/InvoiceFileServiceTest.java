package com.frescuratotal.erp.sales.service;

import com.frescuratotal.erp.sales.model.InvoiceLine;
import com.frescuratotal.erp.sales.model.SaleReceipt;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvoiceFileServiceTest {
    @Test
    public void createsReadableQrAndPdfInvoiceFiles() throws Exception {
        String invoiceNumber = "TEST-INVOICE-QR-PDF";
        String cufe = "0123456789abcdef";
        Path outputDirectory = Path.of("output", "invoices");
        Path pdf = outputDirectory.resolve(invoiceNumber + ".pdf");
        Path qr = outputDirectory.resolve(invoiceNumber + "-qr.png");
        SaleReceipt receipt = sampleReceipt(invoiceNumber, cufe);

        try {
            SaleReceipt generated = new InvoiceFileService().createFiles(receipt);

            assertTrue(Files.size(generated.pdfPath()) > 0);
            assertTrue(Files.size(generated.qrPath()) > 0);
            assertTrue(new String(Files.readAllBytes(generated.pdfPath()), 0, 4)
                    .startsWith("%PDF"));
            BufferedImage image = ImageIO.read(generated.qrPath().toFile());
            String decoded = new MultiFormatReader().decode(new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(image)))).getText();
            assertEquals("https://frescuratotal.local/verify?cufe=" + cufe, decoded);
        } finally {
            Files.deleteIfExists(pdf);
            Files.deleteIfExists(qr);
        }
    }

    private SaleReceipt sampleReceipt(String invoiceNumber, String cufe) {
        InvoiceLine line = new InvoiceLine("Demo product", 1001, BigDecimal.ONE,
                new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("0.19"), new BigDecimal("19.00"),
                new BigDecimal("100.00"), new BigDecimal("119.00"));
        return new SaleReceipt(1, "SALE-TEST", invoiceNumber, cufe, "Test Customer",
                "TEST-1", List.of(line), new BigDecimal("100.00"), BigDecimal.ZERO,
                new BigDecimal("19.00"), new BigDecimal("119.00"), null, null);
    }
}
