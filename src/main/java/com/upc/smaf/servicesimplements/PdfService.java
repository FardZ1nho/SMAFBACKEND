package com.upc.smaf.servicesimplements;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.upc.smaf.entities.Cotizacion;
import com.upc.smaf.entities.CotizacionDetalle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    // ==================== DEFINICIÓN DE COLORES Y FUENTES MODERNAS ====================
    private static final Color PRIMARY_COLOR = new Color(40, 40, 40); // Gris oscuro para texto principal
    private static final Color SECONDARY_COLOR = new Color(80, 80, 80); // Gris medio para etiquetas
    private static final Color ACCENT_COLOR = new Color(0, 80, 160); // Azul para destacar totales
    private static final Color BORDER_COLOR = new Color(230, 230, 230); // Gris muy claro para bordes suaves
    private static final Color HEADER_BG_COLOR = new Color(248, 248, 248); // Fondo casi blanco para encabezados

    private final Font fontTitle = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
    private final Font fontSubTitle = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
    private final Font fontEmpresa = new Font(Font.HELVETICA, 10, Font.NORMAL, PRIMARY_COLOR);
    private final Font fontLabel = new Font(Font.HELVETICA, 9, Font.BOLD, SECONDARY_COLOR);
    private final Font fontValue = new Font(Font.HELVETICA, 9, Font.NORMAL, PRIMARY_COLOR);
    private final Font fontTableHeader = new Font(Font.HELVETICA, 9, Font.BOLD, PRIMARY_COLOR);
    private final Font fontTableBody = new Font(Font.HELVETICA, 9, Font.NORMAL, PRIMARY_COLOR);
    private final Font fontTotalLabel = new Font(Font.HELVETICA, 10, Font.BOLD, PRIMARY_COLOR);
    private final Font fontTotalValue = new Font(Font.HELVETICA, 12, Font.BOLD, ACCENT_COLOR);
    private final Font fontFooter = new Font(Font.HELVETICA, 8, Font.NORMAL, SECONDARY_COLOR);
    private final Font fontFooterBold = new Font(Font.HELVETICA, 8, Font.BOLD, SECONDARY_COLOR);

    public byte[] generarCotizacionPDF(Cotizacion cot) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Márgenes más amplios para un diseño más "aireado"
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Pie de página con numeración
            writer.setPageEvent(new PdfPageEventHelper() {
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    Phrase footer = new Phrase(String.format("Página %d", writer.getPageNumber()), fontFooter);
                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            footer,
                            (document.right() - document.left()) / 2 + document.leftMargin(),
                            document.bottom() - 20, 0);
                }
            });

            document.open();

            // ================= 1. CABECERA (Logo, Info, RUC) =================
            PdfPTable tableHeader = new PdfPTable(3);
            tableHeader.setWidthPercentage(100);
            tableHeader.setWidths(new float[]{3f, 5f, 3f});
            tableHeader.setSpacingAfter(30f);

            // LOGO
            PdfPCell cellLogo = new PdfPCell();
            cellLogo.setBorder(Rectangle.NO_BORDER);
            cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            try {
                ClassPathResource resource = new ClassPathResource("static/smaflogo.jpg");
                Image img = Image.getInstance(resource.getURL());
                img.scaleToFit(150, 75);
                cellLogo.addElement(img);
            } catch (Exception e) {
                cellLogo.addElement(new Paragraph("SMAF", fontTitle));
            }
            tableHeader.addCell(cellLogo);

            // INFO EMPRESA
            PdfPCell cellEmpresa = new PdfPCell();
            cellEmpresa.setBorder(Rectangle.NO_BORDER);
            cellEmpresa.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cellEmpresa.setPaddingLeft(20f);
            cellEmpresa.addElement(new Paragraph("SMAF S.A.C.", fontSubTitle));
            cellEmpresa.addElement(new Paragraph("JR LAS CALENDULAS 692, SAN JUAN DE LURIGANCHO", fontEmpresa));
            cellEmpresa.addElement(new Paragraph("LIMA - LIMA", fontEmpresa));
            cellEmpresa.addElement(new Paragraph("Telf: (01) 6818768 - Cel: 949812503", fontEmpresa));
            cellEmpresa.addElement(new Paragraph("Email: ventas@smafsac.com", fontEmpresa));
            tableHeader.addCell(cellEmpresa);

            // RECUADRO RUC MODERNIZADO
            PdfPTable rucTable = new PdfPTable(1);
            rucTable.setWidthPercentage(100);

            PdfPCell cellRucHeader = crearCeldaRuc("R.U.C. 20511021180", fontSubTitle, false);
            cellRucHeader.setPaddingTop(10f);
            rucTable.addCell(cellRucHeader);

            PdfPCell cellRucType = crearCeldaRuc("COTIZACIÓN", fontTitle, true);
            rucTable.addCell(cellRucType);

            String numStr = (cot.getSerie() != null ? cot.getSerie() : "C001") + "-" +
                    (cot.getNumero() != null ? cot.getNumero() : "000000");
            PdfPCell cellRucNumber = crearCeldaRuc("N° " + numStr, fontSubTitle, false);
            cellRucNumber.setPaddingBottom(10f);
            rucTable.addCell(cellRucNumber);

            PdfPCell mainRucCell = new PdfPCell(rucTable);
            mainRucCell.setBorderColor(BORDER_COLOR);
            mainRucCell.setBorderWidth(1f);
            tableHeader.addCell(mainRucCell);

            document.add(tableHeader);

            // ================= 2. DATOS DEL CLIENTE (Alineación Perfecta) =================
            // Usamos una tabla de 4 columnas para alinear etiquetas y valores
            PdfPTable tableInfo = new PdfPTable(4);
            tableInfo.setWidthPercentage(100);
            tableInfo.setWidths(new float[]{2f, 5f, 2.5f, 3f}); // Anchos relativos
            tableInfo.setSpacingAfter(25f);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaEmision = cot.getFechaEmision() != null ? cot.getFechaEmision().format(formatter) : "-";
            String fechaVenc = cot.getFechaVencimiento() != null ? cot.getFechaVencimiento().format(formatter) : "-";
            String moneda = cot.getMoneda() != null ? (cot.getMoneda().equals("PEN") ? "Soles" : "Dólares") : "-";

            // Fila 1
            agregarInfo(tableInfo, "Cliente:", cot.getCliente().getNombreCompleto());
            agregarInfo(tableInfo, "Fecha Emisión:", fechaEmision);
            // Fila 2
            agregarInfo(tableInfo, "RUC/DNI:", cot.getCliente().getNumeroDocumento());
            agregarInfo(tableInfo, "Válido Hasta:", fechaVenc);
            // Fila 3
            agregarInfo(tableInfo, "Dirección:", cot.getCliente().getDireccion());
            agregarInfo(tableInfo, "Moneda:", moneda);
            // Fila 4
            agregarInfo(tableInfo, "Forma Pago:", "A tratar");
            agregarInfo(tableInfo, "Vendedor:", "Comercial");

            document.add(tableInfo);

            // ================= 3. TABLA DE PRODUCTOS MODERNA =================
            PdfPTable tablaDetalles = new PdfPTable(6);
            tablaDetalles.setWidthPercentage(100);
            tablaDetalles.setWidths(new float[]{1f, 1.2f, 7f, 1.8f, 1.5f, 1.8f});
            tablaDetalles.setHeaderRows(1);

            // Encabezados con fondo suave
            String[] headers = {"CANT.", "UNIDAD", "DESCRIPCIÓN", "P.UNIT", "DTO.", "TOTAL"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, fontTableHeader));
                c.setBackgroundColor(HEADER_BG_COLOR);
                c.setBorderColor(BORDER_COLOR);
                c.setBorderWidthBottom(1f);
                c.setBorder(Rectangle.BOTTOM);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(8f);
                tablaDetalles.addCell(c);
            }

            // Detalles con solo líneas horizontales
            DecimalFormat df = new DecimalFormat("#,##0.00");
            for (CotizacionDetalle det : cot.getDetalles()) {
                tablaDetalles.addCell(celdaCuerpo(det.getCantidad().toString(), Element.ALIGN_CENTER));
                tablaDetalles.addCell(celdaCuerpo("UND", Element.ALIGN_CENTER));

                PdfPCell cellDesc = celdaCuerpo(det.getProducto().getNombre(), Element.ALIGN_LEFT);
                tablaDetalles.addCell(cellDesc);

                tablaDetalles.addCell(celdaCuerpo(df.format(det.getPrecioUnitario()), Element.ALIGN_RIGHT));
                tablaDetalles.addCell(celdaCuerpo("0.00", Element.ALIGN_RIGHT));
                tablaDetalles.addCell(celdaCuerpo(df.format(det.getImporte()), Element.ALIGN_RIGHT));
            }

            // Rellenar filas vacías para estética
            for(int i=0; i < (8 - cot.getDetalles().size()); i++){
                for(int j=0; j<6; j++) {
                    tablaDetalles.addCell(celdaCuerpo(" ", Element.ALIGN_CENTER));
                }
            }

            document.add(tablaDetalles);

            // ================= 4. TOTALES (Moderno y Destacado) =================
            PdfPTable tableTotales = new PdfPTable(2);
            tableTotales.setWidthPercentage(35);
            tableTotales.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableTotales.setSpacingBefore(15f);

            agregarTotal(tableTotales, "Op. Gravadas:", cot.getSubTotal(), fontLabel, fontValue, df);
            agregarTotal(tableTotales, "IGV (18%):", cot.getIgv(), fontLabel, fontValue, df);

            // Total final destacado con color de acento y mayor tamaño
            PdfPCell cellTotalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR:", fontTotalLabel));
            cellTotalLabel.setBorderColor(BORDER_COLOR);
            cellTotalLabel.setBorder(Rectangle.TOP);
            cellTotalLabel.setPadding(10f);
            cellTotalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableTotales.addCell(cellTotalLabel);

            String simbolo = cot.getMoneda().equals("PEN") ? "S/ " : "$ ";
            PdfPCell cellTotalValue = new PdfPCell(new Phrase(simbolo + df.format(cot.getTotal()), fontTotalValue));
            cellTotalValue.setBorderColor(BORDER_COLOR);
            cellTotalValue.setBorder(Rectangle.TOP);
            cellTotalValue.setPadding(10f);
            cellTotalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tableTotales.addCell(cellTotalValue);

            document.add(tableTotales);

            // ================= 5. PIE DE PÁGINA =================
            document.add(new Paragraph("\n"));

            PdfPTable tableFooter = new PdfPTable(1);
            tableFooter.setWidthPercentage(100);
            tableFooter.setSpacingBefore(30f);

            PdfPCell cellFooter = new PdfPCell();
            cellFooter.setBorderColor(BORDER_COLOR);
            cellFooter.setBorder(Rectangle.TOP);
            cellFooter.setPaddingTop(15f);

            cellFooter.addElement(new Paragraph("CUENTAS BANCARIAS:", fontFooterBold));

            Paragraph pCuentas = new Paragraph();
            pCuentas.setLeading(15f);
            pCuentas.add(new Phrase("• BBVA SOLES: ", fontFooterBold));
            pCuentas.add(new Phrase("0011-0128-0100016385-65  /  ", fontFooter));
            pCuentas.add(new Phrase("CCI: ", fontFooterBold));
            pCuentas.add(new Phrase("011-128-000100016385-65\n", fontFooter));

            pCuentas.add(new Phrase("• BBVA DÓLARES: ", fontFooterBold));
            pCuentas.add(new Phrase("0011-0128-0100033441-65  /  ", fontFooter));
            pCuentas.add(new Phrase("CCI: ", fontFooterBold));
            pCuentas.add(new Phrase("011-128-000100033441-65\n", fontFooter));

            pCuentas.add(new Phrase("• BANCO DE LA NACIÓN SOLES: ", fontFooterBold));
            pCuentas.add(new Phrase("00-004-024-710", fontFooter));
            cellFooter.addElement(pCuentas);

            tableFooter.addCell(cellFooter);
            document.add(tableFooter);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar PDF: " + e.getMessage());
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    // Crea celdas para el cuadro del RUC
    private PdfPCell crearCeldaRuc(String texto, Font font, boolean esTitulo) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorder(Rectangle.NO_BORDER);
        if (esTitulo) {
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setPadding(8f);
        }
        return cell;
    }

    // Agrega un par Etiqueta-Valor a la tabla de información (alineación perfecta)
    private void agregarInfo(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fontLabel));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cLabel.setPaddingRight(10f);
        cLabel.setPaddingBottom(6f);
        table.addCell(cLabel);

        PdfPCell cValue = new PdfPCell(new Phrase(value != null ? value : "-", fontValue));
        cValue.setBorder(Rectangle.NO_BORDER);
        cValue.setHorizontalAlignment(Element.ALIGN_LEFT);
        cValue.setPaddingBottom(6f);
        table.addCell(cValue);
    }

    // Crea celdas para el cuerpo de la tabla de productos (solo bordes inferiores suaves)
    private PdfPCell celdaCuerpo(String texto, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fontTableBody));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(BORDER_COLOR);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPadding(8f);
        return cell;
    }

    // Agrega filas a la tabla de totales
    private void agregarTotal(PdfPTable table, String label, java.math.BigDecimal val, Font fLbl, Font fVal, DecimalFormat df) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, fLbl));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cLabel.setPadding(5f);
        table.addCell(cLabel);

        PdfPCell cValue = new PdfPCell(new Phrase(df.format(val), fVal));
        cValue.setBorder(Rectangle.NO_BORDER);
        cValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cValue.setPadding(5f);
        table.addCell(cValue);
    }
}