package ru.ivanov.diplom.inventory_system.dto.equipment;

import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;

public record EquipmentRegistrationResponse(
        EquipmentResponse equipment,
        DocumentResponse document
) {
}
