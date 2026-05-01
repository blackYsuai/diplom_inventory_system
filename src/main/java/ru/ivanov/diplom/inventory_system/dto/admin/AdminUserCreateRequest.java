package ru.ivanov.diplom.inventory_system.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

import java.util.List;

public record AdminUserCreateRequest(

        Long employeeId,

        String lastName,

        String firstName,

        String middleName,

        String position,

        String phone,

        @Email(message = "Некорректный email")
        String email,

        Long departmentId,

        String username,

        @NotNull(message = "Роль пользователя обязательна")
        UserRole role,

        List<String> permissionCodes
) {
}