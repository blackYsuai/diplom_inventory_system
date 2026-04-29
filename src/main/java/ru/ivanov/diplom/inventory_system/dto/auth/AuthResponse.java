package ru.ivanov.diplom.inventory_system.dto.auth;

public record AuthResponse(
        String username,
        String role,
        String fullName
) {
}
