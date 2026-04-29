package ru.ivanov.diplom.inventory_system.dto.exporting;


import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportStatus;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;

import java.time.LocalDateTime;

public record ExportHistoryResponse(
        Long id,
        LocalDateTime exportedAt,
        ExportStatus status,
        String resultMessage,
        TargetAccountingSystem targetSystem,
        ExportDataType exportType,
        String fileName,
        Long documentId,
        String documentNumber
) {
}
