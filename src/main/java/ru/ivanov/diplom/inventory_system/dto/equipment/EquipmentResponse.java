package ru.ivanov.diplom.inventory_system.dto.equipment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentResponse(
        Long id,
        String inventoryNumber,
        String name,
        String model,
        String serialNumber,
        LocalDate purchaseDate,
        LocalDate commissioningDate,
        BigDecimal initialCost,
        Integer usefulLifeMonths,
        String description,
        Long categoryId,
        String categoryName,
        Long statusId,
        String statusName,
        Long locationId,
        String locationName,
        Long responsibleEmployeeId,
        String responsibleEmployeeFullName
) {
}
