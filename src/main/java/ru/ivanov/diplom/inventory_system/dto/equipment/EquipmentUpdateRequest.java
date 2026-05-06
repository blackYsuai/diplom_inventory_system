package ru.ivanov.diplom.inventory_system.dto.equipment;

public record EquipmentUpdateRequest(
        String name,
        String model,
        String description,
        Long categoryId
) {
}
