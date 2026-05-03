package ru.ivanov.diplom.inventory_system.dto.auth;


import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Логин не должен быть пустым")
        String username,

        @NotBlank(message = "Пароль не должен быть пустым")
        String password
) {
}
