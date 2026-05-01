package ru.ivanov.diplom.inventory_system.dto.admin.reference;

import jakarta.validation.constraints.NotBlank;

public record AdminReferenceItemRequest(

        @NotBlank(message = "Наименование обязательно")
        String name,

        String description
) {
}
