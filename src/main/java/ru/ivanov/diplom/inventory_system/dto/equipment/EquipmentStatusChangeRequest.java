package ru.ivanov.diplom.inventory_system.dto.equipment;

import jakarta.validation.constraints.NotNull;

public record EquipmentStatusChangeRequest(
        @NotNull(message = "Статус оборудования обязателен")
        Long statusId,

        String note
) {
}
