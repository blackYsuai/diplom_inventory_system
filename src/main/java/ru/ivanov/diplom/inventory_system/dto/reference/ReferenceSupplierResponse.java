package ru.ivanov.diplom.inventory_system.dto.reference;

public record ReferenceSupplierResponse(
        Long id,
        String name,
        String inn,
        String contactPerson,
        String phone,
        String email
) {
}
