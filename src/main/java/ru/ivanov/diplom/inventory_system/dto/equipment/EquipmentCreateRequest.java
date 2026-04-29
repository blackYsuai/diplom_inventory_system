package ru.ivanov.diplom.inventory_system.dto.equipment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentCreateRequest(

        @NotBlank(message = "Инвентарный номер обязателен")
        String inventoryNumber,

        @NotBlank(message = "Наименование оборудования обязательно")
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

        @NotNull(message = "Категория оборудования обязательна")
        Long categoryId,

        @NotNull(message = "Статус оборудования обязателен")
        Long statusId,

        @NotNull(message = "Местоположение обязательно")
        Long locationId,

        @NotNull(message = "Ответственный сотрудник обязателен")
        Long responsibleEmployeeId,

        Long supplierId
) {
}
