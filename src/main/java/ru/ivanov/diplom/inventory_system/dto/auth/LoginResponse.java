package ru.ivanov.diplom.inventory_system.dto.auth;


import java.util.List;

public record LoginResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String role,
        String fullName,
        Boolean mustChangePassword,
        List<String> permissions
) {
}
