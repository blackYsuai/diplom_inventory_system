package ru.ivanov.diplom.inventory_system.dto.admin;

import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        UserRole role,
        Boolean active,
        Boolean mustChangePassword,
        LocalDateTime passwordExpiresAt,
        LocalDateTime createdAt,

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
