package ru.ivanov.diplom.inventory_system.dto.document;

import java.time.LocalDate;
import java.util.List;

public record DocumentResponse(
        Long id,
        String documentNumber,
        LocalDate documentDate,
        String documentTypeCode,
        String documentTypeName,
        String comment,
        Long createdByUserId,
        String createdByUsername,
        List<DocumentItemResponse> items
) {
}
