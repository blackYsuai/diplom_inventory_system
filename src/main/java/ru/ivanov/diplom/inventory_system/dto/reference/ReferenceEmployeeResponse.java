package ru.ivanov.diplom.inventory_system.dto.reference;

public record ReferenceEmployeeResponse(
        Long id,
        String fullName,
        String position,
        String phone,
        String email,
        Long departmentId,
        String departmentName
) {
}
