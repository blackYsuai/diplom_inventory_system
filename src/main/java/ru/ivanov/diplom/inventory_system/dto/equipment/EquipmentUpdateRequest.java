package ru.ivanov.diplom.inventory_system.dto.equipment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentUpdateRequest(
        String inventoryNumber,
        String name,
        String model,
        String serialNumber,
        LocalDate purchaseDate,
        LocalDate commissioningDate,

        @DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной")
        BigDecimal initialCost,

        @Positive(message = "Срок полезного использования должен быть положительным")
        Integer usefulLifeMonths,

        String description,

        Long categoryId
) {
}
