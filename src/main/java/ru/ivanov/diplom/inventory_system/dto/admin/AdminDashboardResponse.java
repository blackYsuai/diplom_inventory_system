package ru.ivanov.diplom.inventory_system.dto.admin;

public record AdminDashboardResponse(
        long usersCount,
        long activeUsersCount,
        long equipmentCount,
        long documentsCount,
        long exportRecordsCount
) {
}
