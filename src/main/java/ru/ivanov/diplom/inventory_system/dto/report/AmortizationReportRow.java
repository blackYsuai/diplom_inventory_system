package ru.ivanov.diplom.inventory_system.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmortizationReportRow(
        Long equipmentId,
        String inventoryNumber,
        String equipmentName,
        String model,
        String serialNumber,

        String statusName,
        String locationName,
        String responsibleEmployeeFullName,

        BigDecimal initialCost,
        LocalDate commissioningDate,
        Integer usefulLifeMonths,

        Long monthsInUse,
        BigDecimal monthlyAmortization,
        BigDecimal accumulatedAmortization,
        BigDecimal residualValue
) {
}
