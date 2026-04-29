package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportResponse;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentMovementReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentStateReportRow;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.entity.enums.DocumentTypeCode;
import ru.ivanov.diplom.inventory_system.repository.DocumentItemRepository;
import ru.ivanov.diplom.inventory_system.repository.EquipmentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final EquipmentRepository equipmentRepository;
    private final DocumentItemRepository documentItemRepository;

    @Transactional(readOnly = true)
    public List<EquipmentStateReportRow> getEquipmentStateReport(
            Long statusId,
            Long categoryId,
            Long locationId,
            Long responsibleEmployeeId
    ) {
        return equipmentRepository.findAll()
                .stream()
                .filter(equipment -> statusId == null
                        || getId(equipment.getStatus()) != null
                        && Objects.equals(getId(equipment.getStatus()), statusId))
                .filter(equipment -> categoryId == null
                        || getId(equipment.getCategory()) != null
                        && Objects.equals(getId(equipment.getCategory()), categoryId))
                .filter(equipment -> locationId == null
                        || getId(equipment.getLocation()) != null
                        && Objects.equals(getId(equipment.getLocation()), locationId))
                .filter(equipment -> responsibleEmployeeId == null
                        || getId(equipment.getResponsibleEmployee()) != null
                        && Objects.equals(getId(equipment.getResponsibleEmployee()), responsibleEmployeeId))
                .map(this::toEquipmentStateReportRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentMovementReportRow> getEquipmentMovementReport(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long equipmentId,
            String documentTypeCode
    ) {
        List<String> documentTypes = resolveDocumentTypes(documentTypeCode);

        return documentItemRepository.findAllForMovementReport(documentTypes)
                .stream()
                .filter(item -> equipmentId == null
                        || item.getEquipment() != null
                        && Objects.equals(item.getEquipment().getId(), equipmentId))
                .filter(item -> dateFrom == null
                        || item.getDocument() != null
                        && item.getDocument().getDocumentDate() != null
                        && !item.getDocument().getDocumentDate().isBefore(dateFrom))
                .filter(item -> dateTo == null
                        || item.getDocument() != null
                        && item.getDocument().getDocumentDate() != null
                        && !item.getDocument().getDocumentDate().isAfter(dateTo))
                .sorted(Comparator
                        .comparing((DocumentItem item) -> item.getDocument().getDocumentDate()).reversed()
                        .thenComparing(item -> item.getDocument().getId(), Comparator.reverseOrder()))
                .map(this::toEquipmentMovementReportRow)
                .toList();
    }

    @Transactional(readOnly = true)
    public AmortizationReportResponse getAmortizationReport(
            LocalDate reportDate,
            Long statusId,
            Long categoryId,
            Long locationId,
            Long responsibleEmployeeId
    ) {
        LocalDate actualReportDate = reportDate == null ? LocalDate.now() : reportDate;

        List<AmortizationReportRow> rows = equipmentRepository.findAll()
                .stream()
                .filter(equipment -> statusId == null
                        || getId(equipment.getStatus()) != null
                        && Objects.equals(getId(equipment.getStatus()), statusId))
                .filter(equipment -> categoryId == null
                        || getId(equipment.getCategory()) != null
                        && Objects.equals(getId(equipment.getCategory()), categoryId))
                .filter(equipment -> locationId == null
                        || getId(equipment.getLocation()) != null
                        && Objects.equals(getId(equipment.getLocation()), locationId))
                .filter(equipment -> responsibleEmployeeId == null
                        || getId(equipment.getResponsibleEmployee()) != null
                        && Objects.equals(getId(equipment.getResponsibleEmployee()), responsibleEmployeeId))
                .map(equipment -> toAmortizationReportRow(equipment, actualReportDate))
                .toList();

        BigDecimal totalInitialCost = rows.stream()
                .map(AmortizationReportRow::initialCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAccumulatedAmortization = rows.stream()
                .map(AmortizationReportRow::accumulatedAmortization)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalResidualValue = rows.stream()
                .map(AmortizationReportRow::residualValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AmortizationReportResponse(
                actualReportDate,
                "Линейный метод",
                "Ликвидационная стоимость в рамках расчета не учитывается и принимается равной 0.",
                totalInitialCost,
                totalAccumulatedAmortization,
                totalResidualValue,
                rows
        );
    }

    private AmortizationReportRow toAmortizationReportRow(Equipment equipment, LocalDate reportDate) {
        BigDecimal initialCost = normalizeMoney(equipment.getInitialCost());
        Integer usefulLifeMonths = equipment.getUsefulLifeMonths();
        LocalDate commissioningDate = equipment.getCommissioningDate();

        BigDecimal monthlyAmortization = calculateMonthlyAmortization(initialCost, usefulLifeMonths);
        Long monthsInUse = calculateMonthsInUse(commissioningDate, reportDate, usefulLifeMonths);
        BigDecimal accumulatedAmortization = calculateAccumulatedAmortization(
                monthlyAmortization,
                monthsInUse,
                initialCost
        );
        BigDecimal residualValue = initialCost.subtract(accumulatedAmortization);

        if (residualValue.compareTo(BigDecimal.ZERO) < 0) {
            residualValue = BigDecimal.ZERO;
        }

        return new AmortizationReportRow(
                equipment.getId(),
                equipment.getInventoryNumber(),
                equipment.getName(),
                equipment.getModel(),
                equipment.getSerialNumber(),

                equipment.getStatus() != null ? equipment.getStatus().getName() : null,
                getLocationName(equipment.getLocation()),
                equipment.getResponsibleEmployee() != null
                        ? equipment.getResponsibleEmployee().getFullName()
                        : null,

                initialCost,
                commissioningDate,
                usefulLifeMonths,

                monthsInUse,
                monthlyAmortization,
                accumulatedAmortization,
                residualValue
        );
    }

    private BigDecimal calculateMonthlyAmortization(BigDecimal initialCost, Integer usefulLifeMonths) {
        if (initialCost == null
                || initialCost.compareTo(BigDecimal.ZERO) <= 0
                || usefulLifeMonths == null
                || usefulLifeMonths <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return initialCost.divide(
                BigDecimal.valueOf(usefulLifeMonths),
                2,
                RoundingMode.HALF_UP
        );
    }

    private Long calculateMonthsInUse(
            LocalDate commissioningDate,
            LocalDate reportDate,
            Integer usefulLifeMonths
    ) {
        if (commissioningDate == null
                || reportDate == null
                || usefulLifeMonths == null
                || usefulLifeMonths <= 0) {
            return 0L;
        }

        LocalDate amortizationStartMonth = commissioningDate
                .plusMonths(1)
                .withDayOfMonth(1);

        LocalDate reportMonth = reportDate.withDayOfMonth(1);

        if (reportMonth.isBefore(amortizationStartMonth)) {
            return 0L;
        }

        long calculatedMonths = ChronoUnit.MONTHS.between(
                amortizationStartMonth,
                reportMonth
        ) + 1;

        return Math.min(calculatedMonths, usefulLifeMonths.longValue());
    }

    private BigDecimal calculateAccumulatedAmortization(
            BigDecimal monthlyAmortization,
            Long monthsInUse,
            BigDecimal initialCost
    ) {
        if (monthlyAmortization == null || monthsInUse == null || monthsInUse <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal accumulated = monthlyAmortization.multiply(BigDecimal.valueOf(monthsInUse));

        if (initialCost != null && accumulated.compareTo(initialCost) > 0) {
            return initialCost.setScale(2, RoundingMode.HALF_UP);
        }

        return accumulated.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private List<String> resolveDocumentTypes(String documentTypeCode) {
        if (documentTypeCode != null && !documentTypeCode.isBlank()) {
            return List.of(documentTypeCode.trim());
        }

        return List.of(
                DocumentTypeCode.RECEIPT.getCode(),
                DocumentTypeCode.MOVEMENT.getCode(),
                DocumentTypeCode.WRITE_OFF.getCode()
        );
    }

    private EquipmentStateReportRow toEquipmentStateReportRow(Equipment equipment) {
        StorageLocation location = equipment.getLocation();
        Department department = location != null ? location.getDepartment() : null;
        Employee employee = equipment.getResponsibleEmployee();

        return new EquipmentStateReportRow(
                equipment.getId(),
                equipment.getInventoryNumber(),
                equipment.getName(),
                equipment.getModel(),
                equipment.getSerialNumber(),

                equipment.getCategory() != null ? equipment.getCategory().getName() : null,
                equipment.getStatus() != null ? equipment.getStatus().getName() : null,

                location != null ? location.getName() : null,
                location != null ? location.getBuilding() : null,
                location != null ? location.getRoom() : null,
                department != null ? department.getName() : null,

                employee != null ? employee.getId() : null,
                employee != null ? employee.getFullName() : null,
                employee != null ? employee.getPosition() : null,

                equipment.getInitialCost(),
                equipment.getPurchaseDate(),
                equipment.getCommissioningDate(),
                equipment.getUsefulLifeMonths()
        );
    }

    private EquipmentMovementReportRow toEquipmentMovementReportRow(DocumentItem item) {
        InventoryDocument document = item.getDocument();
        Equipment equipment = item.getEquipment();

        return new EquipmentMovementReportRow(
                document != null ? document.getId() : null,
                document != null ? document.getDocumentNumber() : null,
                document != null ? document.getDocumentDate() : null,
                document != null && document.getDocumentType() != null
                        ? document.getDocumentType().getCode()
                        : null,
                document != null && document.getDocumentType() != null
                        ? document.getDocumentType().getName()
                        : null,

                item.getId(),

                equipment != null ? equipment.getId() : null,
                equipment != null ? equipment.getInventoryNumber() : null,
                equipment != null ? equipment.getName() : null,

                getLocationName(item.getFromLocation()),
                getLocationName(item.getToLocation()),

                item.getFromEmployee() != null ? item.getFromEmployee().getFullName() : null,
                item.getToEmployee() != null ? item.getToEmployee().getFullName() : null,

                equipment != null && equipment.getStatus() != null
                        ? equipment.getStatus().getName()
                        : null,

                item.getNote()
        );
    }

    private String getLocationName(StorageLocation location) {
        if (location == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();

        if (location.getName() != null) {
            result.append(location.getName());
        }

        if (location.getBuilding() != null && !location.getBuilding().isBlank()) {
            result.append(", ").append(location.getBuilding());
        }

        if (location.getRoom() != null && !location.getRoom().isBlank()) {
            result.append(", каб. ").append(location.getRoom());
        }

        return result.toString();
    }

    private Long getId(EquipmentStatus status) {
        return status == null ? null : status.getId();
    }

    private Long getId(EquipmentCategory category) {
        return category == null ? null : category.getId();
    }

    private Long getId(StorageLocation location) {
        return location == null ? null : location.getId();
    }

    private Long getId(Employee employee) {
        return employee == null ? null : employee.getId();
    }
}
