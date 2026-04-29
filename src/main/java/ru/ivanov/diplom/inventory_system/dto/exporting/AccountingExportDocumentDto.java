package ru.ivanov.diplom.inventory_system.dto.exporting;

import java.time.LocalDate;
import java.util.List;

public record AccountingExportDocumentDto(
        Long id,
        String documentNumber,
        LocalDate documentDate,
        String documentTypeCode,
        String documentTypeName,
        String comment,
        String createdByUsername,
        List<String> suppliers,
        List<AccountingExportDocumentItemDto> items
) {
}
