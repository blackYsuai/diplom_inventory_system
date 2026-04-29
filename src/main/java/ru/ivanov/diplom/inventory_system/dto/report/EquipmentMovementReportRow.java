package ru.ivanov.diplom.inventory_system.dto.report;

import java.time.LocalDate;

public record EquipmentMovementReportRow(
        Long documentId,
        String documentNumber,
        LocalDate documentDate,
        String documentTypeCode,
        String documentTypeName,

        Long documentItemId,

        Long equipmentId,
        String inventoryNumber,
        String equipmentName,

        String fromLocationName,
        String toLocationName,

        String fromEmployeeFullName,
        String toEmployeeFullName,

        String currentStatusName,
        String note
) {
}
