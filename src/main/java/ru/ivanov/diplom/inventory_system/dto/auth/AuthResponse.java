package ru.ivanov.diplom.inventory_system.dto.auth;

import java.util.List;

public record AuthResponse(
        Long userId,
        String username,
        String role,
        String fullName,
        Boolean mustChangePassword,
        List<String> permissions
) {
}
