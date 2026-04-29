package ru.ivanov.diplom.inventory_system.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AmortizationReportResponse(
        LocalDate reportDate,
        String amortizationMethod,
        String note,
        BigDecimal totalInitialCost,
        BigDecimal totalAccumulatedAmortization,
        BigDecimal totalResidualValue,
        List<AmortizationReportRow> rows
) {
}
