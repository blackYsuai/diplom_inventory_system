package ru.ivanov.diplom.inventory_system.dto.equipment;

import java.time.LocalDate;
import java.util.List;

public record EquipmentHistoryItemResponse(
        Long documentId,
        String documentNumber,
        LocalDate documentDate,
        String documentTypeCode,
        String documentTypeName,

        Long documentItemId,

        String fromLocationName,
        String toLocationName,

        String fromEmployeeFullName,
        String toEmployeeFullName,

        List<String> writeOffReasons,
        String note
) {
}