package ru.ivanov.diplom.inventory_system.dto.admin.employee;

public record AdminEmployeeResponse(
        Long id,
        String lastName,
        String firstName,
        String middleName,
        String fullName,
        String position,
        String phone,
        String email,
        Long departmentId,
        String departmentName,
        Boolean hasUserAccount
) {
}
