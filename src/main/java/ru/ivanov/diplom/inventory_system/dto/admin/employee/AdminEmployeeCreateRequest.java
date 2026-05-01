package ru.ivanov.diplom.inventory_system.dto.admin.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminEmployeeCreateRequest(

        @NotBlank(message = "Фамилия обязательна")
        String lastName,

        @NotBlank(message = "Имя обязательно")
        String firstName,

        String middleName,

        String position,

        String phone,

        @Email(message = "Некорректный email")
        String email,

        @NotNull(message = "Подразделение обязательно")
        Long departmentId
) {
}
