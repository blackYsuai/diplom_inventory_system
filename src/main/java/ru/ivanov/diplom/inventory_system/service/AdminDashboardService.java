package ru.ivanov.diplom.inventory_system.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.admin.AdminDashboardResponse;
import ru.ivanov.diplom.inventory_system.repository.AccountingExportRepository;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import ru.ivanov.diplom.inventory_system.repository.EquipmentRepository;
import ru.ivanov.diplom.inventory_system.repository.InventoryDocumentRepository;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final AppUserRepository appUserRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final AccountingExportRepository accountingExportRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long usersCount = appUserRepository.count();
        long activeUsersCount = appUserRepository.findAll()
                .stream()
                .filter(user -> Boolean.TRUE.equals(user.getActive()))
                .count();

        return new AdminDashboardResponse(
                usersCount,
                activeUsersCount,
                equipmentRepository.count(),
                inventoryDocumentRepository.count(),
                accountingExportRepository.count()
        );
    }
}
