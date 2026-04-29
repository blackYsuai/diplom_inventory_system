package ru.ivanov.diplom.inventory_system.dto.document;

public record DocumentItemResponse(
        Long id,
        Long equipmentId,
        String equipmentInventoryNumber,
        String equipmentName,
        Long fromLocationId,
        String fromLocationName,
        Long toLocationId,
        String toLocationName,
        Long fromEmployeeId,
        String fromEmployeeFullName,
        Long toEmployeeId,
        String toEmployeeFullName,
        String note
) {
}
