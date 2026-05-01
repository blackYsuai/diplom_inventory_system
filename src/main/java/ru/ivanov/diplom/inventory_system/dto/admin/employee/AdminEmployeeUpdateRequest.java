package ru.ivanov.diplom.inventory_system.dto.admin.employee;


import jakarta.validation.constraints.Email;

public record AdminEmployeeUpdateRequest(
        String lastName,
        String firstName,
        String middleName,
        String position,
        String phone,

        @Email(message = "Некорректный email")
        String email,

        Long departmentId
) {
}
