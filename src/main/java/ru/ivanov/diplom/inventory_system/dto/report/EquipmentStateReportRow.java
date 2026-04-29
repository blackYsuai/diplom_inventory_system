package ru.ivanov.diplom.inventory_system.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EquipmentStateReportRow(
        Long equipmentId,
        String inventoryNumber,
        String equipmentName,
        String model,
        String serialNumber,

        String categoryName,
        String statusName,

        String locationName,
        String building,
        String room,
        String departmentName,

        Long responsibleEmployeeId,
        String responsibleEmployeeFullName,
        String responsibleEmployeePosition,

        BigDecimal initialCost,
        LocalDate purchaseDate,
        LocalDate commissioningDate,
        Integer usefulLifeMonths
) {
}