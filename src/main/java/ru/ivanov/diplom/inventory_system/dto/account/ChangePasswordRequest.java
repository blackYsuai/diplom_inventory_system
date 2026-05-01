package ru.ivanov.diplom.inventory_system.dto.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "Текущий пароль обязателен")
        String currentPassword,

        @NotBlank(message = "Новый пароль обязателен")
        @Size(min = 6, message = "Новый пароль должен содержать не менее 6 символов")
        String newPassword,

        @NotBlank(message = "Подтверждение пароля обязательно")
        String repeatNewPassword
) {
}
