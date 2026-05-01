package ru.ivanov.diplom.inventory_system.dto.admin;

import jakarta.validation.constraints.Email;
import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

import java.util.List;

public record AdminUserUpdateRequest(
        String lastName,
        String firstName,
        String middleName,
        String position,
        String phone,

        @Email(message = "Некорректный email")
        String email,

        Long departmentId,
        UserRole role,
        Boolean active,
        List<String> permissionCodes
) {
}
