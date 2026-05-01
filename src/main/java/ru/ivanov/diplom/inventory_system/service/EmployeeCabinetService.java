package ru.ivanov.diplom.inventory_system.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentShortResponse;
import ru.ivanov.diplom.inventory_system.dto.employee.*;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.entity.enums.UserRole;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.mapper.DocumentMapper;
import ru.ivanov.diplom.inventory_system.mapper.EquipmentMapper;
import ru.ivanov.diplom.inventory_system.repository.AppUserRepository;
import ru.ivanov.diplom.inventory_system.repository.DocumentItemRepository;
import ru.ivanov.diplom.inventory_system.repository.EquipmentRepository;
import ru.ivanov.diplom.inventory_system.repository.InventoryDocumentRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeCabinetService {
    private final CurrentUserService currentUserService;
    private final AppUserRepository appUserRepository;
    private final EquipmentRepository equipmentRepository;
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;

    private final EquipmentMapper equipmentMapper;
    private final DocumentMapper documentMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public EmployeeDashboardResponse getDashboard() {
        AppUser user = currentUserService.getCurrentUser();
        Employee employee = user.getEmployee();

        List<Equipment> equipment = equipmentRepository
                .findAllByResponsibleEmployeeIdWithDetails(employee.getId());

        List<DocumentItem> documentItems = documentItemRepository
                .findAllForEmployeeCabinet(employee.getId());

        return new EmployeeDashboardResponse(
                toProfileResponse(user),
                equipment.size(),
                countUniqueDocuments(documentItems),
                buildAvailableActions(user)
        );
    }

    @Transactional(readOnly = true)
    public EmployeeProfileResponse getProfile() {
        AppUser user = currentUserService.getCurrentUser();
        return toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getMyEquipment() {
        AppUser user = currentUserService.getCurrentUser();
        Long employeeId = user.getEmployee().getId();

        return equipmentRepository.findAllByResponsibleEmployeeIdWithDetails(employeeId)
                .stream()
                .map(equipmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentShortResponse> getMyDocuments() {
        AppUser user = currentUserService.getCurrentUser();
        Long employeeId = user.getEmployee().getId();

        List<DocumentItem> items = documentItemRepository.findAllForEmployeeCabinet(employeeId);

        Map<Long, List<DocumentItem>> groupedByDocument = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getDocument().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return groupedByDocument.values()
                .stream()
                .map(documentItems -> {
                    InventoryDocument document = documentItems.get(0).getDocument();
                    return documentMapper.toShortResponse(document, documentItems);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getMyDocumentById(Long documentId) {
        checkDocumentAccess(documentId);

        InventoryDocument document = inventoryDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Документ с id " + documentId + " не найден"
                ));

        List<DocumentItem> items = documentItemRepository.findAllByDocumentId(documentId);

        return documentMapper.toResponse(document, items);
    }

    @Transactional(readOnly = true)
    public void checkDocumentAccess(Long documentId) {
        AppUser user = currentUserService.getCurrentUser();

        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        Long employeeId = user.getEmployee().getId();

        boolean hasAccess = documentItemRepository.existsByDocumentIdAndEmployeeId(
                documentId,
                employeeId
        );

        if (!hasAccess) {
            throw new ResourceNotFoundException(
                    "Документ с id " + documentId + " не найден для текущего сотрудника"
            );
        }
    }

    @Transactional
    public ChangeOwnPasswordResponse changeOwnPassword(ChangeOwnPasswordRequest request) {
        if (!request.newPassword().equals(request.repeatNewPassword())) {
            throw new BadRequestException("Новый пароль и подтверждение не совпадают");
        }

        AppUser currentUser = currentUserService.getCurrentUser();

        AppUser user = appUserRepository.findByIdWithDetails(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Текущий пользователь не найден"
                ));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Текущий пароль указан неверно");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        user.setPasswordExpiresAt(null);

        appUserRepository.save(user);

        return new ChangeOwnPasswordResponse("Пароль успешно изменен");
    }

    private EmployeeProfileResponse toProfileResponse(AppUser user) {
        Employee employee = user.getEmployee();
        Department department = employee != null ? employee.getDepartment() : null;

        List<String> permissionCodes = user.getPermissions() == null
                ? List.of()
                : user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .sorted()
                .toList();

        return new EmployeeProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getActive(),
                user.getMustChangePassword(),

                employee != null ? employee.getId() : null,
                employee != null ? employee.getFullName() : null,
                employee != null ? employee.getPosition() : null,
                employee != null ? employee.getPhone() : null,
                employee != null ? employee.getEmail() : null,

                department != null ? department.getId() : null,
                department != null ? department.getName() : null,

                permissionCodes
        );
    }

    private long countUniqueDocuments(List<DocumentItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        return items.stream()
                .map(item -> item.getDocument().getId())
                .distinct()
                .count();
    }

    private List<AvailableActionResponse> buildAvailableActions(AppUser user) {
        Set<String> permissions = user.getPermissions() == null
                ? Set.of()
                : user.getPermissions()
                .stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        if (user.getRole() == UserRole.ADMIN) {
            permissions = Set.of(
                    "EQUIPMENT_VIEW",
                    "EQUIPMENT_CREATE",
                    "EQUIPMENT_MOVE",
                    "EQUIPMENT_WRITE_OFF",
                    "DOCUMENT_VIEW",
                    "REPORT_VIEW",
                    "EXPORT_DATA",
                    "USER_MANAGE",
                    "EQUIPMENT_UPDATE",
                    "REFERENCE_MANAGE"
            );
        }

        List<AvailableActionResponse> actions = new ArrayList<>();

        addActionIfAllowed(
                actions,
                permissions,
                "EQUIPMENT_VIEW",
                "Просмотр оборудования",
                "/api/equipment"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "EQUIPMENT_CREATE",
                "Регистрация оборудования",
                "/api/equipment"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "EQUIPMENT_MOVE",
                "Перемещение оборудования",
                "/api/operations/move"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "EQUIPMENT_WRITE_OFF",
                "Списание оборудования",
                "/api/operations/write-off"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "DOCUMENT_VIEW",
                "Просмотр документов",
                "/api/documents"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "REPORT_VIEW",
                "Формирование отчетов",
                "/api/reports"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "EXPORT_DATA",
                "Экспорт данных",
                "/api/export/accounting"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "EQUIPMENT_UPDATE",
                "Редактирование оборудования",
                "/api/equipment/{id}"
        );

        addActionIfAllowed(
                actions,
                permissions,
                "REFERENCE_MANAGE",
                "Управление справочниками",
                "/api/admin/references"
        );

        return actions;
    }

    private void addActionIfAllowed(
            List<AvailableActionResponse> actions,
            Set<String> permissions,
            String permissionCode,
            String name,
            String endpoint
    ) {
        if (permissions.contains(permissionCode)) {
            actions.add(new AvailableActionResponse(permissionCode, name, endpoint));
        }
    }
}
