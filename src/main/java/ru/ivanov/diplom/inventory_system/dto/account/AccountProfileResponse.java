package ru.ivanov.diplom.inventory_system.dto.account;

import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

import java.util.List;

public record AccountProfileResponse(
        Long userId,
        String username,
        UserRole role,
        Boolean active,
        Boolean mustChangePassword,

        Long employeeId,
        String fullName,
        String position,
        String phone,
        String email,

        Long departmentId,
        String departmentName,

        List<String> permissionCodes
) {
}
