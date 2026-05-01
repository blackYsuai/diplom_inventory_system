package ru.ivanov.diplom.inventory_system.dto.employee;

public record AvailableActionResponse(
        String permissionCode,
        String name,
        String endpoint
) {
}
