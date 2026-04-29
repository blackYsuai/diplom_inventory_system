package ru.ivanov.diplom.inventory_system.dto.exporting;

import java.math.BigDecimal;
import java.util.List;

public record AccountingExportDocumentItemDto(
        Long equipmentId,
        String inventoryNumber,
        String equipmentName,
        String model,
        String serialNumber,
        BigDecimal initialCost,
        String statusName,
        String fromLocation,
        String toLocation,
        String fromEmployee,
        String toEmployee,
        List<String> writeOffReasons,
        String note
) {
}
