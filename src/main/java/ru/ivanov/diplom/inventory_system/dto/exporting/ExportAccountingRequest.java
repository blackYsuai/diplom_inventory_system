package ru.ivanov.diplom.inventory_system.dto.exporting;

import jakarta.validation.constraints.NotNull;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;

import java.time.LocalDate;
import java.util.List;

public record ExportAccountingRequest(

        @NotNull(message = "Целевая система обязательна")
        TargetAccountingSystem targetSystem,

        @NotNull(message = "Тип экспортируемых данных обязателен")
        ExportDataType exportType,

        LocalDate dateFrom,

        LocalDate dateTo,

        LocalDate reportDate,

        List<Long> documentIds,

        List<Long> equipmentIds,

        Long statusId,

        Long categoryId,

        Long locationId,

        Long responsibleEmployeeId,

        String endpointUrl
) {
}