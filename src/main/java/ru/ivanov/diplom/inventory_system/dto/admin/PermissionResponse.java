package ru.ivanov.diplom.inventory_system.dto.admin;

public record PermissionResponse(
        Long id,
        String code,
        String name,
        String description
) {
}
