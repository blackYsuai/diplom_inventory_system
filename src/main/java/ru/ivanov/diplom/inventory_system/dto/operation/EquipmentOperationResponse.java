package ru.ivanov.diplom.inventory_system.dto.operation;

import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;

public record EquipmentOperationResponse(
        EquipmentResponse equipment,
        DocumentResponse document
) {
}
