package ru.ivanov.diplom.inventory_system.dto.document;

import java.time.LocalDate;
import java.util.List;

public record DocumentShortResponse(
        Long id,
        String documentNumber,
        LocalDate documentDate,
        String documentTypeCode,
        String documentTypeName,
        String comment,
        String createdByUsername,
        List<String> equipmentNames
) {
}
