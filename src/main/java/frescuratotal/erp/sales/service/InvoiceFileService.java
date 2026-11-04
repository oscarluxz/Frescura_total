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
                    receipt.total(), pdf, qr, receipt.getCompany(), receipt.getIssuedAt(), receipt.getCustomerAddress());
        } catch (Exception e) {
            deleteIfExists(pdf, e);
            deleteIfExists(qr, e);
            throw new IllegalStateException("No se pudieron crear los archivos de la factura.", e);
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
            throw new IllegalStateException("No se pudieron eliminar los archivos de la factura tras revertir la transacción.", e);
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
            document.add(new Paragraph(receipt.getCompany().getName()));
            document.add(new Paragraph("NIT: " + receipt.getCompany().getNit()));
            document.add(new Paragraph("Dirección: " + receipt.getCompany().getAddress()));
            document.add(new Paragraph("Contacto: " + receipt.getCompany().getPhone() + " | " + receipt.getCompany().getEmail()));
            document.add(new Paragraph("Factura electrónica simulada: " + receipt.invoiceNumber()));
            document.add(new Paragraph("Fecha de emisión (Bogotá): " + receipt.getIssuedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            document.add(new Paragraph("Venta: " + receipt.saleNumber()));
            document.add(new Paragraph("Cliente: " + receipt.customerName()));
            document.add(new Paragraph("Documento: " + receipt.customerDocument()));
            document.add(new Paragraph("Dirección del cliente: " + receipt.getCustomerAddress()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.addCell("Producto / lote");
            table.addCell("Cantidad");
            table.addCell("Precio unitario");
            table.addCell("Descuento");
            table.addCell("IVA");
            table.addCell("Total línea");
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
            document.add(new Paragraph("Descuento: " + receipt.discount()));
            document.add(new Paragraph("IVA (19%): " + receipt.tax()));
            document.add(new Paragraph("Total: " + receipt.total()));
            document.add(new Paragraph("CUFE simulado: " + receipt.cufe()));
            document.add(new Paragraph("Documento académico de prueba. Sin validación ni autorización de la DIAN."));
            document.add(new Paragraph(" "));
            document.add(Image.getInstance(qr.toAbsolutePath().toString()));
        } catch (Exception e) {
            throw new IOException("No se pudo crear el PDF de la factura.", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }
}
