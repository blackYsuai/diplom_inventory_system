package ru.ivanov.diplom.inventory_system.dto.admin;

public record AdminUserCreateResponse(
        AdminUserResponse user,
        String temporaryPassword
) {
}
