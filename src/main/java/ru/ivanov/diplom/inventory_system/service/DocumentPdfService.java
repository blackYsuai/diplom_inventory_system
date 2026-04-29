package ru.ivanov.diplom.inventory_system.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.repository.DocumentItemRepository;
import ru.ivanov.diplom.inventory_system.repository.InventoryDocumentRepository;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentPdfService {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;

    @Transactional(readOnly = true)
    public byte[] generateDocumentPdf(Long documentId) {
        InventoryDocument document = inventoryDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Документ с id " + documentId + " не найден"
                ));

        List<DocumentItem> items = documentItemRepository.findAllByDocumentId(document.getId());

        String html = buildHtml(document, items);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            File fontFile = new File("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf");
            if (fontFile.exists()) {
                builder.useFont(fontFile, "DejaVu Sans");
            }

            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new RuntimeException("Ошибка при формировании PDF-документа", exception);
        }
    }

    public String buildFileName(Long documentId) {
        InventoryDocument document = inventoryDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Документ с id " + documentId + " не найден"
                ));

        return document.getDocumentNumber()
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                + ".pdf";
    }

    private String buildHtml(InventoryDocument document, List<DocumentItem> items) {
        String title = resolveDocumentTitle(document);
        String supplierBlock = buildSupplierBlock(document);

        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4;
                            margin: 20mm;
                        }

                        body {
                            font-family: 'DejaVu Sans', sans-serif;
                            font-size: 12px;
                            color: #111;
                        }

                        h1 {
                            text-align: center;
                            font-size: 18px;
                            margin-bottom: 6px;
                        }

                        .subtitle {
                            text-align: center;
                            font-size: 13px;
                            margin-bottom: 24px;
                        }

                        .info {
                            margin-bottom: 18px;
                            line-height: 1.5;
                        }

                        .info-row {
                            margin-bottom: 4px;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 12px;
                            margin-bottom: 20px;
                        }

                        th, td {
                            border: 1px solid #333;
                            padding: 6px;
                            vertical-align: top;
                        }

                        th {
                            text-align: center;
                            background: #eeeeee;
                        }

                        .text-right {
                            text-align: right;
                        }

                        .comment {
                            margin-top: 12px;
                            margin-bottom: 24px;
                        }

                        .signature-table {
                            width: 100%%;
                            margin-top: 50px;
                            border-collapse: collapse;
                        }

                        .signature-table td {
                            border: none;
                            padding-top: 24px;
                            width: 50%%;
                            text-align: center;
                        }

                        .line {
                            border-top: 1px solid #000;
                            display: inline-block;
                            width: 220px;
                            padding-top: 4px;
                        }

                        .small {
                            font-size: 10px;
                        }
                    </style>
                </head>
                <body>
                    <h1>%s</h1>
                    <div class="subtitle">№ %s от %s</div>

                    <div class="info">
                        <div class="info-row"><strong>Тип документа:</strong> %s</div>
                        <div class="info-row"><strong>Сформировал пользователь:</strong> %s</div>
                        %s
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>№</th>
                                <th>Инвентарный номер</th>
                                <th>Оборудование</th>
                                <th>Серийный номер</th>
                                <th>Исходное размещение</th>
                                <th>Новое размещение</th>
                                <th>Ответственный до</th>
                                <th>Ответственный после</th>
                                <th>Причина / примечание</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <div class="comment">
                        <strong>Комментарий:</strong> %s
                    </div>

                    <div>
                        Настоящий документ сформирован автоматизированной системой инвентарного учета оборудования
                        и подтверждает выполнение учетной операции.
                    </div>

                    <table class="signature-table">
                        <tr>
                            <td>
                                <span class="line">Передал</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                            <td>
                                <span class="line">Принял</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                escape(title),
                escape(document.getDocumentNumber()),
                escape(formatDate(document)),
                escape(getDocumentTypeName(document)),
                escape(getCreatedByUsername(document)),
                supplierBlock,
                buildItemsRows(items),
                escape(nullToDash(document.getComment()))
        );
    }

    private String buildItemsRows(List<DocumentItem> items) {
        if (items == null || items.isEmpty()) {
            return """
                    <tr>
                        <td colspan="9" style="text-align:center;">Позиции документа отсутствуют</td>
                    </tr>
                    """;
        }

        StringBuilder rows = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            DocumentItem item = items.get(i);
            Equipment equipment = item.getEquipment();

            rows.append("""
                    <tr>
                        <td class="text-right">%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    i + 1,
                    escape(equipment != null ? nullToDash(equipment.getInventoryNumber()) : "-"),
                    escape(buildEquipmentName(equipment)),
                    escape(equipment != null ? nullToDash(equipment.getSerialNumber()) : "-"),
                    escape(getLocationName(item.getFromLocation())),
                    escape(getLocationName(item.getToLocation())),
                    escape(getEmployeeName(item.getFromEmployee())),
                    escape(getEmployeeName(item.getToEmployee())),
                    escape(buildReasonAndNote(item))
            ));
        }

        return rows.toString();
    }

    private String buildSupplierBlock(InventoryDocument document) {
        if (document.getSuppliers() == null || document.getSuppliers().isEmpty()) {
            return "";
        }

        String suppliers = document.getSuppliers()
                .stream()
                .map(supplier -> supplier.getName() + formatInn(supplier))
                .collect(Collectors.joining(", "));

        return "<div class=\"info-row\"><strong>Поставщик:</strong> "
                + escape(suppliers)
                + "</div>";
    }

    private String formatInn(Supplier supplier) {
        if (supplier == null || supplier.getInn() == null || supplier.getInn().isBlank()) {
            return "";
        }

        return " (ИНН " + supplier.getInn() + ")";
    }

    private String resolveDocumentTitle(InventoryDocument document) {
        String code = document.getDocumentType() != null
                ? document.getDocumentType().getCode()
                : "";

        return switch (code) {
            case "RECEIPT" -> "АКТ ПОСТУПЛЕНИЯ ОБОРУДОВАНИЯ";
            case "MOVEMENT" -> "АКТ ПРИЕМА-ПЕРЕДАЧИ ОБОРУДОВАНИЯ";
            case "WRITE_OFF" -> "АКТ СПИСАНИЯ ОБОРУДОВАНИЯ";
            case "EXPORT" -> "ДОКУМЕНТ ЭКСПОРТА ДАННЫХ";
            default -> "УЧЕТНЫЙ ДОКУМЕНТ";
        };
    }

    private String buildEquipmentName(Equipment equipment) {
        if (equipment == null) {
            return "-";
        }

        StringBuilder result = new StringBuilder();

        if (equipment.getName() != null) {
            result.append(equipment.getName());
        }

        if (equipment.getModel() != null && !equipment.getModel().isBlank()) {
            result.append(" ").append(equipment.getModel());
        }

        return result.isEmpty() ? "-" : result.toString();
    }

    private String buildReasonAndNote(DocumentItem item) {
        String reasons = "";

        if (item.getWriteOffReasons() != null && !item.getWriteOffReasons().isEmpty()) {
            reasons = item.getWriteOffReasons()
                    .stream()
                    .map(WriteOffReason::getName)
                    .collect(Collectors.joining(", "));
        }

        String note = item.getNote();

        if ((reasons == null || reasons.isBlank()) && (note == null || note.isBlank())) {
            return "-";
        }

        if (reasons == null || reasons.isBlank()) {
            return note;
        }

        if (note == null || note.isBlank()) {
            return reasons;
        }

        return reasons + ". " + note;
    }

    private String getDocumentTypeName(InventoryDocument document) {
        if (document.getDocumentType() == null) {
            return "-";
        }

        return nullToDash(document.getDocumentType().getName());
    }

    private String getCreatedByUsername(InventoryDocument document) {
        if (document.getCreatedByUser() == null) {
            return "-";
        }

        return nullToDash(document.getCreatedByUser().getUsername());
    }

    private String getLocationName(StorageLocation location) {
        if (location == null) {
            return "-";
        }

        StringBuilder result = new StringBuilder();

        if (location.getName() != null) {
            result.append(location.getName());
        }

        if (location.getBuilding() != null && !location.getBuilding().isBlank()) {
            result.append(", ").append(location.getBuilding());
        }

        if (location.getRoom() != null && !location.getRoom().isBlank()) {
            result.append(", каб. ").append(location.getRoom());
        }

        return result.isEmpty() ? "-" : result.toString();
    }

    private String getEmployeeName(Employee employee) {
        if (employee == null) {
            return "-";
        }

        return nullToDash(employee.getFullName());
    }

    private String formatDate(InventoryDocument document) {
        if (document.getDocumentDate() == null) {
            return "-";
        }

        return document.getDocumentDate().format(DATE_FORMATTER);
    }

    private String nullToDash(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
