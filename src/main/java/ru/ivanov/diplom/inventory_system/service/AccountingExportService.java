package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.exporting.*;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportResponse;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentMovementReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentStateReportRow;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportStatus;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.repository.AccountingExportRepository;
import ru.ivanov.diplom.inventory_system.repository.DocumentItemRepository;
import ru.ivanov.diplom.inventory_system.repository.InventoryDocumentRepository;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AccountingExportService {
    private static final String SOURCE_SYSTEM = "InventorySystem";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private static final DateTimeFormatter FILE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;
    private final AccountingExportRepository accountingExportRepository;
    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public AccountingExportPackage previewExport(ExportAccountingRequest request) {
        validateRequest(request);
        return buildExportPackage(request);
    }

    @Transactional
    public ExportDownloadResult downloadExport(ExportAccountingRequest request) {
        validateRequest(request);

        AccountingExportPackage exportPackage = buildExportPackage(request);
        String fileName = buildFileName(request);

        try {
            byte[] content = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exportPackage)
                    .getBytes(StandardCharsets.UTF_8);

            saveSuccessExportHistory(request, fileName, resolveSingleDocument(request));

            return new ExportDownloadResult(content, fileName);
        } catch (Exception exception) {
            saveErrorExportHistory(request, fileName, resolveSingleDocument(request), exception.getMessage());
            throw new RuntimeException("Ошибка при формировании экспортного файла", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<ExportHistoryResponse> getExportHistory() {
        return getExportHistory(null, null, null, null, null);
    }


    @Transactional(readOnly = true)
    public List<ExportHistoryResponse> getExportHistory(
            ExportStatus status,
            ExportDataType exportType,
            TargetAccountingSystem targetSystem,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return accountingExportRepository.findAllByOrderByExportedAtDesc()
                .stream()
                .filter(record -> status == null || record.getStatus() == status)
                .filter(record -> exportType == null || record.getExportType() == exportType)
                .filter(record -> targetSystem == null || record.getTargetSystem() == targetSystem)
                .filter(record -> dateFrom == null
                        || record.getExportedAt() != null
                        && !record.getExportedAt().toLocalDate().isBefore(dateFrom))
                .filter(record -> dateTo == null
                        || record.getExportedAt() != null
                        && !record.getExportedAt().toLocalDate().isAfter(dateTo))
                .map(this::toHistoryResponse)
                .toList();
    }

    @Transactional
    public ExportSendResponse sendExport(ExportAccountingRequest request) {
        validateRequest(request);
        validateEndpointUrl(request.endpointUrl());

        AccountingExportPackage exportPackage = buildExportPackage(request);
        String fileName = buildFileName(request);
        InventoryDocument document = resolveSingleDocument(request);

        try {
            String jsonBody = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exportPackage);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(request.endpointUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;

            if (success) {
                saveSuccessExportHistory(
                        request,
                        fileName,
                        document,
                        "Экспортный пакет успешно отправлен во внешнюю систему. HTTP status: "
                                + response.statusCode()
                );

                return new ExportSendResponse(
                        ExportStatus.SUCCESS,
                        response.statusCode(),
                        "Экспортный пакет успешно отправлен",
                        request.endpointUrl(),
                        response.body()
                );
            }

            saveErrorExportHistory(
                    request,
                    fileName,
                    document,
                    "Внешняя система вернула ошибку. HTTP status: "
                            + response.statusCode()
                            + ". Response: "
                            + response.body()
            );

            return new ExportSendResponse(
                    ExportStatus.ERROR,
                    response.statusCode(),
                    "Внешняя система вернула ошибку",
                    request.endpointUrl(),
                    response.body()
            );

        } catch (Exception exception) {
            saveErrorExportHistory(
                    request,
                    fileName,
                    document,
                    exception.getMessage()
            );

            return new ExportSendResponse(
                    ExportStatus.ERROR,
                    0,
                    "Ошибка при отправке экспортного пакета: " + exception.getMessage(),
                    request.endpointUrl(),
                    null
            );
        }
    }

    private AccountingExportPackage buildExportPackage(ExportAccountingRequest request) {
        Object payload = switch (request.exportType()) {
            case DOCUMENTS -> buildDocumentsPayload(request);
            case EQUIPMENT_STATE -> buildEquipmentStatePayload(request);
            case EQUIPMENT_MOVEMENT -> buildEquipmentMovementPayload(request);
            case AMORTIZATION_REPORT -> buildAmortizationPayload(request);
        };

        return new AccountingExportPackage(
                SOURCE_SYSTEM,
                request.targetSystem(),
                request.exportType(),
                LocalDateTime.now(),
                request.dateFrom(),
                request.dateTo(),
                payload
        );
    }

    private List<AccountingExportDocumentDto> buildDocumentsPayload(ExportAccountingRequest request) {
        List<InventoryDocument> documents = resolveDocuments(request);

        return documents.stream()
                .map(this::toExportDocumentDto)
                .toList();
    }

    private List<EquipmentStateReportRow> buildEquipmentStatePayload(ExportAccountingRequest request) {
        return reportService.getEquipmentStateReport(
                        request.statusId(),
                        request.categoryId(),
                        request.locationId(),
                        request.responsibleEmployeeId()
                )
                .stream()
                .filter(row -> request.equipmentIds() == null
                        || request.equipmentIds().isEmpty()
                        || request.equipmentIds().contains(row.equipmentId()))
                .toList();
    }

    private List<EquipmentMovementReportRow> buildEquipmentMovementPayload(ExportAccountingRequest request) {
        return reportService.getEquipmentMovementReport(
                        request.dateFrom(),
                        request.dateTo(),
                        null,
                        null
                )
                .stream()
                .filter(row -> request.equipmentIds() == null
                        || request.equipmentIds().isEmpty()
                        || request.equipmentIds().contains(row.equipmentId()))
                .toList();
    }

    private AmortizationReportResponse buildAmortizationPayload(ExportAccountingRequest request) {
        AmortizationReportResponse report = reportService.getAmortizationReport(
                request.reportDate(),
                request.statusId(),
                request.categoryId(),
                request.locationId(),
                request.responsibleEmployeeId()
        );

        if (request.equipmentIds() == null || request.equipmentIds().isEmpty()) {
            return report;
        }

        var filteredRows = report.rows()
                .stream()
                .filter(row -> request.equipmentIds().contains(row.equipmentId()))
                .toList();

        var totalInitialCost = filteredRows.stream()
                .map(row -> row.initialCost())
                .filter(Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var totalAccumulatedAmortization = filteredRows.stream()
                .map(row -> row.accumulatedAmortization())
                .filter(Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var totalResidualValue = filteredRows.stream()
                .map(row -> row.residualValue())
                .filter(Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new AmortizationReportResponse(
                report.reportDate(),
                report.amortizationMethod(),
                report.note(),
                totalInitialCost,
                totalAccumulatedAmortization,
                totalResidualValue,
                filteredRows
        );
    }

    private List<InventoryDocument> resolveDocuments(ExportAccountingRequest request) {
        List<InventoryDocument> documents;

        if (request.documentIds() != null && !request.documentIds().isEmpty()) {
            documents = inventoryDocumentRepository.findAllById(request.documentIds());
        } else {
            documents = inventoryDocumentRepository.findAllByOrderByDocumentDateDescIdDesc();
        }

        return documents.stream()
                .filter(document -> request.dateFrom() == null
                        || document.getDocumentDate() != null
                        && !document.getDocumentDate().isBefore(request.dateFrom()))
                .filter(document -> request.dateTo() == null
                        || document.getDocumentDate() != null
                        && !document.getDocumentDate().isAfter(request.dateTo()))
                .filter(document -> request.equipmentIds() == null
                        || request.equipmentIds().isEmpty()
                        || documentContainsEquipment(document, request.equipmentIds()))
                .sorted(Comparator
                        .comparing(InventoryDocument::getDocumentDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(InventoryDocument::getId, Comparator.reverseOrder()))
                .toList();
    }

    private boolean documentContainsEquipment(InventoryDocument document, List<Long> equipmentIds) {
        return documentItemRepository.findAllByDocumentId(document.getId())
                .stream()
                .anyMatch(item -> item.getEquipment() != null
                        && equipmentIds.contains(item.getEquipment().getId()));
    }

    private AccountingExportDocumentDto toExportDocumentDto(InventoryDocument document) {
        List<DocumentItem> items = documentItemRepository.findAllByDocumentId(document.getId());

        List<String> suppliers = document.getSuppliers() == null
                ? List.of()
                : document.getSuppliers()
                .stream()
                .map(Supplier::getName)
                .toList();

        return new AccountingExportDocumentDto(
                document.getId(),
                document.getDocumentNumber(),
                document.getDocumentDate(),
                document.getDocumentType() != null ? document.getDocumentType().getCode() : null,
                document.getDocumentType() != null ? document.getDocumentType().getName() : null,
                document.getComment(),
                document.getCreatedByUser() != null ? document.getCreatedByUser().getUsername() : null,
                suppliers,
                items.stream().map(this::toExportDocumentItemDto).toList()
        );
    }

    private AccountingExportDocumentItemDto toExportDocumentItemDto(DocumentItem item) {
        Equipment equipment = item.getEquipment();

        List<String> reasons = item.getWriteOffReasons() == null
                ? List.of()
                : item.getWriteOffReasons()
                .stream()
                .map(WriteOffReason::getName)
                .toList();

        return new AccountingExportDocumentItemDto(
                equipment != null ? equipment.getId() : null,
                equipment != null ? equipment.getInventoryNumber() : null,
                equipment != null ? equipment.getName() : null,
                equipment != null ? equipment.getModel() : null,
                equipment != null ? equipment.getSerialNumber() : null,
                equipment != null ? equipment.getInitialCost() : null,
                equipment != null && equipment.getStatus() != null ? equipment.getStatus().getName() : null,
                buildLocationName(item.getFromLocation()),
                buildLocationName(item.getToLocation()),
                item.getFromEmployee() != null ? item.getFromEmployee().getFullName() : null,
                item.getToEmployee() != null ? item.getToEmployee().getFullName() : null,
                reasons,
                item.getNote()
        );
    }

    private InventoryDocument resolveSingleDocument(ExportAccountingRequest request) {
        if (request.exportType() != ExportDataType.DOCUMENTS) {
            return null;
        }

        List<InventoryDocument> documents = resolveDocuments(request);

        if (documents.size() == 1) {
            return documents.get(0);
        }

        return null;
    }

    private void saveSuccessExportHistory(
            ExportAccountingRequest request,
            String fileName,
            InventoryDocument document
    ) {
        saveSuccessExportHistory(
                request,
                fileName,
                document,
                "Экспортный файл успешно сформирован"
        );
    }

    private void saveSuccessExportHistory(
            ExportAccountingRequest request,
            String fileName,
            InventoryDocument document,
            String message
    ) {
        AccountingExport exportRecord = AccountingExport.builder()
                .exportedAt(LocalDateTime.now())
                .status(ExportStatus.SUCCESS)
                .resultMessage(message)
                .targetSystem(request.targetSystem())
                .exportType(request.exportType())
                .fileName(fileName)
                .document(document)
                .build();

        accountingExportRepository.save(exportRecord);
    }

    private void validateEndpointUrl(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            throw new BadRequestException("Для отправки данных необходимо указать URL внешней системы");
        }

        if (!endpointUrl.startsWith("http://") && !endpointUrl.startsWith("https://")) {
            throw new BadRequestException("URL внешней системы должен начинаться с http:// или https://");
        }
    }

    private void saveErrorExportHistory(
            ExportAccountingRequest request,
            String fileName,
            InventoryDocument document,
            String errorMessage
    ) {
        AccountingExport exportRecord = AccountingExport.builder()
                .exportedAt(LocalDateTime.now())
                .status(ExportStatus.ERROR)
                .resultMessage("Ошибка формирования экспортного файла: " + errorMessage)
                .targetSystem(request.targetSystem())
                .exportType(request.exportType())
                .fileName(fileName)
                .document(document)
                .build();

        accountingExportRepository.save(exportRecord);
    }

    private ExportHistoryResponse toHistoryResponse(AccountingExport exportRecord) {
        InventoryDocument document = exportRecord.getDocument();

        return new ExportHistoryResponse(
                exportRecord.getId(),
                exportRecord.getExportedAt(),
                exportRecord.getStatus(),
                exportRecord.getResultMessage(),
                exportRecord.getTargetSystem(),
                exportRecord.getExportType(),
                exportRecord.getFileName(),
                document != null ? document.getId() : null,
                document != null ? document.getDocumentNumber() : null
        );
    }

    private String buildFileName(ExportAccountingRequest request) {
        return "accounting-export-"
                + request.targetSystem().name().toLowerCase()
                + "-"
                + request.exportType().name().toLowerCase()
                + "-"
                + LocalDateTime.now().format(FILE_DATE_TIME_FORMATTER)
                + ".json";
    }

    private String buildLocationName(StorageLocation location) {
        if (location == null) {
            return null;
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

        return result.toString();
    }

    private void validateRequest(ExportAccountingRequest request) {
        if (request.dateFrom() != null
                && request.dateTo() != null
                && request.dateTo().isBefore(request.dateFrom())) {
            throw new BadRequestException("Дата окончания периода не может быть раньше даты начала");
        }
    }
}
