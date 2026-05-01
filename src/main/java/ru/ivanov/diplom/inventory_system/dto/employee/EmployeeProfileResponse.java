package ru.ivanov.diplom.inventory_system.dto.employee;

import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;

import java.util.List;

public record EmployeeProfileResponse(
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
