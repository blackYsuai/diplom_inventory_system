package ru.ivanov.diplom.inventory_system.dto.exporting;


import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountingExportPackage(
        String sourceSystem,
        TargetAccountingSystem targetSystem,
        ExportDataType exportType,
        LocalDateTime generatedAt,
        LocalDate dateFrom,
        LocalDate dateTo,
        Object payload
) {
}