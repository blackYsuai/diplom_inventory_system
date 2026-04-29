package ru.ivanov.diplom.inventory_system.dto.operation;

import jakarta.validation.constraints.NotNull;

public record MoveEquipmentRequest(

        @NotNull(message = "Оборудование обязательно")
        Long equipmentId,

        Long toLocationId,

        Long toEmployeeId,

        String note
) {
}
