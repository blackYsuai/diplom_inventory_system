package ru.ivanov.diplom.inventory_system.dto.operation;

import jakarta.validation.constraints.NotNull;

public record WriteOffEquipmentRequest(

        @NotNull(message = "Оборудование обязательно")
        Long equipmentId,

        @NotNull(message = "Причина списания обязательна")
        Long writeOffReasonId,

        String note
) {
}
