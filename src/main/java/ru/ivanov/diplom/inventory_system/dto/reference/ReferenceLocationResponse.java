package ru.ivanov.diplom.inventory_system.dto.reference;

public record ReferenceLocationResponse(
        Long id,
        String name,
        String building,
        String room,
        Long departmentId,
        String departmentName
) {
}
