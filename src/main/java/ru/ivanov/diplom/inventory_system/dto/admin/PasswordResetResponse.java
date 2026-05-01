package ru.ivanov.diplom.inventory_system.dto.admin;

public record PasswordResetResponse(
        Long userId,
        String username,
        String temporaryPassword
) {
}
