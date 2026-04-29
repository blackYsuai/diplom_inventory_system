package ru.ivanov.diplom.inventory_system.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportResponse;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentMovementReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentStateReportRow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportPdfService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final ReportService reportService;

    public byte[] generateAmortizationReportPdf(
            LocalDate reportDate,
            Long statusId,
            Long categoryId,
            Long locationId,
            Long responsibleEmployeeId
    ) {
        AmortizationReportResponse report = reportService.getAmortizationReport(
                reportDate,
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );

        String html = buildAmortizationReportHtml(report);

        return renderPdf(html, "Ошибка при формировании PDF-отчета по амортизации");
    }

    public byte[] generateEquipmentStateReportPdf(
            Long statusId,
            Long categoryId,
            Long locationId,
            Long responsibleEmployeeId
    ) {
        List<EquipmentStateReportRow> rows = reportService.getEquipmentStateReport(
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );

        String html = buildEquipmentStateReportHtml(rows);

        return renderPdf(html, "Ошибка при формировании PDF-отчета о состоянии оборудования");
    }

    public byte[] generateEquipmentMovementReportPdf(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long equipmentId,
            String documentTypeCode
    ) {
        List<EquipmentMovementReportRow> rows = reportService.getEquipmentMovementReport(
                dateFrom,
                dateTo,
                equipmentId,
                documentTypeCode
        );

        String html = buildEquipmentMovementReportHtml(rows, dateFrom, dateTo, documentTypeCode);

        return renderPdf(html, "Ошибка при формировании PDF-отчета о движении оборудования");
    }

    private byte[] renderPdf(String html, String errorMessage) {
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
            throw new RuntimeException(errorMessage, exception);
        }
    }

    private String buildAmortizationReportHtml(AmortizationReportResponse report) {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4 landscape;
                            margin: 14mm;
                        }

                        body {
                            font-family: 'DejaVu Sans', sans-serif;
                            font-size: 10px;
                            color: #111;
                        }

                        h1 {
                            text-align: center;
                            font-size: 18px;
                            margin-bottom: 8px;
                        }

                        .subtitle {
                            text-align: center;
                            margin-bottom: 20px;
                            font-size: 12px;
                        }

                        .info {
                            margin-bottom: 16px;
                            line-height: 1.5;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 12px;
                            margin-bottom: 16px;
                        }

                        th, td {
                            border: 1px solid #333;
                            padding: 5px;
                            vertical-align: top;
                        }

                        th {
                            text-align: center;
                            background: #eeeeee;
                            font-weight: bold;
                        }

                        .text-right {
                            text-align: right;
                        }

                        .summary {
                            margin-top: 16px;
                            width: 55%%;
                            margin-left: auto;
                        }

                        .summary td {
                            font-weight: bold;
                        }

                        .signature-table {
                            width: 100%%;
                            margin-top: 45px;
                            border-collapse: collapse;
                        }

                        .signature-table td {
                            border: none;
                            width: 50%%;
                            text-align: center;
                            padding-top: 25px;
                        }

                        .line {
                            border-top: 1px solid #000;
                            display: inline-block;
                            width: 240px;
                            padding-top: 4px;
                        }

                        .small {
                            font-size: 9px;
                        }
                    </style>
                </head>
                <body>
                    <h1>РАСЧЕТНЫЙ ОТЧЕТ ПО АМОРТИЗАЦИИ ОБОРУДОВАНИЯ</h1>
                    <div class="subtitle">Дата формирования: %s</div>

                    <div class="info">
                        <div><strong>Метод амортизации:</strong> %s</div>
                        <div><strong>Примечание:</strong> %s</div>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>№</th>
                                <th>Инв. номер</th>
                                <th>Оборудование</th>
                                <th>Статус</th>
                                <th>Местоположение</th>
                                <th>Ответственный</th>
                                <th>Дата ввода</th>
                                <th>Стоимость</th>
                                <th>СПИ, мес.</th>
                                <th>Мес. экспл.</th>
                                <th>Мес. аморт.</th>
                                <th>Накопл. аморт.</th>
                                <th>Остаточная стоимость</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <table class="summary">
                        <tr>
                            <td>Итого первоначальная стоимость</td>
                            <td class="text-right">%s руб.</td>
                        </tr>
                        <tr>
                            <td>Итого накопленная амортизация</td>
                            <td class="text-right">%s руб.</td>
                        </tr>
                        <tr>
                            <td>Итого остаточная стоимость</td>
                            <td class="text-right">%s руб.</td>
                        </tr>
                    </table>

                    <table class="signature-table">
                        <tr>
                            <td>
                                <span class="line">Ответственный сотрудник</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                            <td>
                                <span class="line">Главный бухгалтер</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                formatDate(report.reportDate()),
                escape(report.amortizationMethod()),
                escape(report.note()),
                buildAmortizationRows(report),
                formatMoney(report.totalInitialCost()),
                formatMoney(report.totalAccumulatedAmortization()),
                formatMoney(report.totalResidualValue())
        );
    }

    private String buildEquipmentStateReportHtml(List<EquipmentStateReportRow> rows) {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4 landscape;
                            margin: 14mm;
                        }

                        body {
                            font-family: 'DejaVu Sans', sans-serif;
                            font-size: 10px;
                            color: #111;
                        }

                        h1 {
                            text-align: center;
                            font-size: 18px;
                            margin-bottom: 8px;
                        }

                        .subtitle {
                            text-align: center;
                            margin-bottom: 20px;
                            font-size: 12px;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 12px;
                            margin-bottom: 16px;
                        }

                        th, td {
                            border: 1px solid #333;
                            padding: 5px;
                            vertical-align: top;
                        }

                        th {
                            text-align: center;
                            background: #eeeeee;
                            font-weight: bold;
                        }

                        .text-right {
                            text-align: right;
                        }

                        .summary {
                            margin-top: 16px;
                            font-weight: bold;
                        }

                        .signature-table {
                            width: 100%%;
                            margin-top: 45px;
                            border-collapse: collapse;
                        }

                        .signature-table td {
                            border: none;
                            width: 50%%;
                            text-align: center;
                            padding-top: 25px;
                        }

                        .line {
                            border-top: 1px solid #000;
                            display: inline-block;
                            width: 240px;
                            padding-top: 4px;
                        }

                        .small {
                            font-size: 9px;
                        }
                    </style>
                </head>
                <body>
                    <h1>ОТЧЕТ О СОСТОЯНИИ ОБОРУДОВАНИЯ</h1>
                    <div class="subtitle">Дата формирования: %s</div>

                    <table>
                        <thead>
                            <tr>
                                <th>№</th>
                                <th>Инв. номер</th>
                                <th>Оборудование</th>
                                <th>Серийный номер</th>
                                <th>Категория</th>
                                <th>Статус</th>
                                <th>Местоположение</th>
                                <th>Подразделение</th>
                                <th>Ответственный</th>
                                <th>Стоимость</th>
                                <th>Дата ввода</th>
                                <th>СПИ, мес.</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <div class="summary">
                        Всего объектов оборудования: %d
                    </div>

                    <table class="signature-table">
                        <tr>
                            <td>
                                <span class="line">Ответственный сотрудник</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                            <td>
                                <span class="line">Руководитель подразделения</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                formatDate(LocalDate.now()),
                buildEquipmentStateRows(rows),
                rows == null ? 0 : rows.size()
        );
    }

    private String buildEquipmentMovementReportHtml(
            List<EquipmentMovementReportRow> rows,
            LocalDate dateFrom,
            LocalDate dateTo,
            String documentTypeCode
    ) {
        return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        @page {
                            size: A4 landscape;
                            margin: 14mm;
                        }

                        body {
                            font-family: 'DejaVu Sans', sans-serif;
                            font-size: 10px;
                            color: #111;
                        }

                        h1 {
                            text-align: center;
                            font-size: 18px;
                            margin-bottom: 8px;
                        }

                        .subtitle {
                            text-align: center;
                            margin-bottom: 18px;
                            font-size: 12px;
                        }

                        .filters {
                            margin-bottom: 14px;
                            line-height: 1.5;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                            margin-top: 12px;
                            margin-bottom: 16px;
                        }

                        th, td {
                            border: 1px solid #333;
                            padding: 5px;
                            vertical-align: top;
                        }

                        th {
                            text-align: center;
                            background: #eeeeee;
                            font-weight: bold;
                        }

                        .text-right {
                            text-align: right;
                        }

                        .summary {
                            margin-top: 16px;
                            font-weight: bold;
                        }

                        .signature-table {
                            width: 100%%;
                            margin-top: 45px;
                            border-collapse: collapse;
                        }

                        .signature-table td {
                            border: none;
                            width: 50%%;
                            text-align: center;
                            padding-top: 25px;
                        }

                        .line {
                            border-top: 1px solid #000;
                            display: inline-block;
                            width: 240px;
                            padding-top: 4px;
                        }

                        .small {
                            font-size: 9px;
                        }
                    </style>
                </head>
                <body>
                    <h1>ОТЧЕТ О ДВИЖЕНИИ ОБОРУДОВАНИЯ</h1>
                    <div class="subtitle">Дата формирования: %s</div>

                    <div class="filters">
                        <div><strong>Период:</strong> %s</div>
                        <div><strong>Тип документа:</strong> %s</div>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>№</th>
                                <th>Номер документа</th>
                                <th>Дата</th>
                                <th>Тип операции</th>
                                <th>Инв. номер</th>
                                <th>Оборудование</th>
                                <th>Откуда</th>
                                <th>Куда</th>
                                <th>Ответственный до</th>
                                <th>Ответственный после</th>
                                <th>Текущий статус</th>
                                <th>Примечание</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>

                    <div class="summary">
                        Всего операций: %d
                    </div>

                    <table class="signature-table">
                        <tr>
                            <td>
                                <span class="line">Ответственный сотрудник</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                            <td>
                                <span class="line">Руководитель подразделения</span><br/>
                                <span class="small">подпись / расшифровка</span>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                formatDate(LocalDate.now()),
                escape(buildPeriodText(dateFrom, dateTo)),
                escape(nullToDash(documentTypeCode)),
                buildEquipmentMovementRows(rows),
                rows == null ? 0 : rows.size()
        );
    }

    private String buildAmortizationRows(AmortizationReportResponse report) {
        if (report.rows() == null || report.rows().isEmpty()) {
            return """
                    <tr>
                        <td colspan="13" style="text-align:center;">Данные для формирования отчета отсутствуют</td>
                    </tr>
                    """;
        }

        StringBuilder rows = new StringBuilder();

        for (int i = 0; i < report.rows().size(); i++) {
            AmortizationReportRow row = report.rows().get(i);

            rows.append("""
                    <tr>
                        <td class="text-right">%d</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td class="text-right">%s</td>
                        <td class="text-right">%s</td>
                        <td class="text-right">%s</td>
                        <td class="text-right">%s</td>
                        <td class="text-right">%s</td>
                        <td class="text-right">%s</td>
                    </tr>
                    """.formatted(
                    i + 1,
                    escape(nullToDash(row.inventoryNumber())),
                    escape(buildEquipmentName(row)),
                    escape(nullToDash(row.statusName())),
                    escape(nullToDash(row.locationName())),
                    escape(nullToDash(row.responsibleEmployeeFullName())),
                    formatDate(row.commissioningDate()),
                    formatMoney(row.initialCost()),
                    row.usefulLifeMonths() == null ? "-" : row.usefulLifeMonths(),
                    row.monthsInUse() == null ? "-" : row.monthsInUse(),
                    formatMoney(row.monthlyAmortization()),
                    formatMoney(row.accumulatedAmortization()),
                    formatMoney(row.residualValue())
            ));
        }

        return rows.toString();
    }

    private String buildEquipmentStateRows(List<EquipmentStateReportRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return """
                    <tr>
                        <td colspan="12" style="text-align:center;">Данные для формирования отчета отсутствуют</td>
                    </tr>
                    """;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < rows.size(); i++) {
            EquipmentStateReportRow row = rows.get(i);

            result.append("""
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
                        <td class="text-right">%s</td>
                        <td>%s</td>
                        <td class="text-right">%s</td>
                    </tr>
                    """.formatted(
                    i + 1,
                    escape(nullToDash(row.inventoryNumber())),
                    escape(buildEquipmentName(row.equipmentName(), row.model())),
                    escape(nullToDash(row.serialNumber())),
                    escape(nullToDash(row.categoryName())),
                    escape(nullToDash(row.statusName())),
                    escape(buildLocation(row.locationName(), row.building(), row.room())),
                    escape(nullToDash(row.departmentName())),
                    escape(nullToDash(row.responsibleEmployeeFullName())),
                    formatMoney(row.initialCost()),
                    formatDate(row.commissioningDate()),
                    row.usefulLifeMonths() == null ? "-" : row.usefulLifeMonths()
            ));
        }

        return result.toString();
    }

    private String buildEquipmentMovementRows(List<EquipmentMovementReportRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return """
                    <tr>
                        <td colspan="12" style="text-align:center;">Данные для формирования отчета отсутствуют</td>
                    </tr>
                    """;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < rows.size(); i++) {
            EquipmentMovementReportRow row = rows.get(i);

            result.append("""
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
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                    """.formatted(
                    i + 1,
                    escape(nullToDash(row.documentNumber())),
                    formatDate(row.documentDate()),
                    escape(nullToDash(row.documentTypeName())),
                    escape(nullToDash(row.inventoryNumber())),
                    escape(nullToDash(row.equipmentName())),
                    escape(nullToDash(row.fromLocationName())),
                    escape(nullToDash(row.toLocationName())),
                    escape(nullToDash(row.fromEmployeeFullName())),
                    escape(nullToDash(row.toEmployeeFullName())),
                    escape(nullToDash(row.currentStatusName())),
                    escape(nullToDash(row.note()))
            ));
        }

        return result.toString();
    }

    private String buildEquipmentName(AmortizationReportRow row) {
        StringBuilder result = new StringBuilder();

        if (row.equipmentName() != null && !row.equipmentName().isBlank()) {
            result.append(row.equipmentName());
        }

        if (row.model() != null && !row.model().isBlank()) {
            result.append(" ").append(row.model());
        }

        return result.length() == 0 ? "-" : result.toString();
    }

    private String buildEquipmentName(String name, String model) {
        StringBuilder result = new StringBuilder();

        if (name != null && !name.isBlank()) {
            result.append(name);
        }

        if (model != null && !model.isBlank()) {
            result.append(" ").append(model);
        }

        return result.length() == 0 ? "-" : result.toString();
    }

    private String buildLocation(String locationName, String building, String room) {
        StringBuilder result = new StringBuilder();

        if (locationName != null && !locationName.isBlank()) {
            result.append(locationName);
        }

        if (building != null && !building.isBlank()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(building);
        }

        if (room != null && !room.isBlank()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append("каб. ").append(room);
        }

        return result.length() == 0 ? "-" : result.toString();
    }

    private String buildPeriodText(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null && dateTo == null) {
            return "не задан";
        }

        if (dateFrom != null && dateTo != null) {
            return "с " + formatDate(dateFrom) + " по " + formatDate(dateTo);
        }

        if (dateFrom != null) {
            return "с " + formatDate(dateFrom);
        }

        return "по " + formatDate(dateTo);
    }

    private String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }

        return date.format(DATE_FORMATTER);
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.toPlainString();
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