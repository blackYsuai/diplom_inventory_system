package ru.ivanov.diplom.inventory_system.dto.admin.reference;

import jakarta.validation.constraints.NotBlank;

public record AdminSupplierRequest(

        @NotBlank(message = "Наименование поставщика обязательно")
        String name,

        String inn,

        String contactPerson,

        String phone,

        String email
) {
}
