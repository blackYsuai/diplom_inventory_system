package ru.ivanov.diplom.inventory_system.dto.admin.reference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminLocationRequest(

        @NotBlank(message = "Наименование местоположения обязательно")
        String name,

        String building,

        String room,

        @NotNull(message = "Подразделение обязательно")
        Long departmentId
) {
}
