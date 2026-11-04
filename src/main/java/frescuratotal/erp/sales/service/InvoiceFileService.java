package frescuratotal.erp.sales.service;

import frescuratotal.erp.sales.model.SaleReceipt;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvoiceFileService {
    private static final Path OUTPUT_DIR = Path.of("output", "invoices");

    public SaleReceipt createFiles(SaleReceipt receipt) {
        Path qr = OUTPUT_DIR.resolve(receipt.invoiceNumber() + "-qr.png");
        Path pdf = OUTPUT_DIR.resolve(receipt.invoiceNumber() + ".pdf");
        try {
            Files.createDirectories(OUTPUT_DIR);
            String verificationUrl = "https://frescuratotal.local/verify?cufe=" + receipt.cufe();
            BitMatrix matrix = new MultiFormatWriter().encode(
                    verificationUrl, BarcodeFormat.QR_CODE, 260, 260);
            MatrixToImageWriter.writeToPath(matrix, "PNG", qr);
            createPdf(receipt, qr, pdf);
            return new SaleReceipt(receipt.saleId(), receipt.saleNumber(), receipt.invoiceNumber(),
                    receipt.cufe(), receipt.customerName(), receipt.customerDocument(), receipt.lines(),
                    receipt.subtotal(), receipt.discount(), receipt.tax(),
                    receipt.total(), pdf, qr);
        } catch (Exception e) {
            deleteIfExists(pdf, e);
            deleteIfExists(qr, e);
            throw new IllegalStateException("Invoice files could not be created.", e);
        }
    }

    public void deleteFiles(SaleReceipt receipt) {
        if (receipt == null) {
            return;
        }
        try {
            if (receipt.pdfPath() != null) {
                Files.deleteIfExists(receipt.pdfPath());
            }
            if (receipt.qrPath() != null) {
                Files.deleteIfExists(receipt.qrPath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not remove invoice files after transaction rollback.", e);
        }
    }

    private void deleteIfExists(Path file, Exception original) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException cleanupError) {
            original.addSuppressed(cleanupError);
        }
    }

    private void createPdf(SaleReceipt receipt, Path qr, Path pdf) throws IOException {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, Files.newOutputStream(pdf));
            document.open();
            document.add(new Paragraph("FRESCURA TOTAL S.A.S."));
            document.add(new Paragraph("Electronic invoice: " + receipt.invoiceNumber()));
            document.add(new Paragraph("Sale: " + receipt.saleNumber()));
            document.add(new Paragraph("Customer: " + receipt.customerName()));
            document.add(new Paragraph("Document: " + receipt.customerDocument()));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Product / lot");
            table.addCell("Quantity");
            table.addCell("Unit price");
            table.addCell("Discount");
            table.addCell("IVA");
            table.addCell("Line total");
            for (var line : receipt.lines()) {
                table.addCell(line.productName() + " / " + line.lotId());
                table.addCell(line.quantity().toPlainString());
                table.addCell(line.unitPrice().toPlainString());
                table.addCell(line.discount().toPlainString());
                table.addCell(line.tax().toPlainString());
                table.addCell(line.total().toPlainString());
            }
            document.add(table);
            document.add(new Paragraph("Subtotal: " + receipt.subtotal()));
            document.add(new Paragraph("Discount: " + receipt.discount()));
            document.add(new Paragraph("IVA (19%): " + receipt.tax()));
            document.add(new Paragraph("Total: " + receipt.total()));
            document.add(new Paragraph("Simulated CUFE: " + receipt.cufe()));
            document.add(new Paragraph(" "));
            document.add(Image.getInstance(qr.toAbsolutePath().toString()));
        } catch (Exception e) {
            throw new IOException("Could not create invoice PDF.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
}
