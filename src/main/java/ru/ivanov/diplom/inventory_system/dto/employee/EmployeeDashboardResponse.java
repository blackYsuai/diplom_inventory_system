package ru.ivanov.diplom.inventory_system.dto.employee;

import java.util.List;

public record EmployeeDashboardResponse(
        EmployeeProfileResponse profile,
        long assignedEquipmentCount,
        long relatedDocumentsCount,
        List<AvailableActionResponse> availableActions
) {
}