package ru.ivanov.diplom.inventory_system.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceEmployeeResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceItemResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceLocationResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceSupplierResponse;
import ru.ivanov.diplom.inventory_system.entity.Department;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import ru.ivanov.diplom.inventory_system.entity.StorageLocation;
import ru.ivanov.diplom.inventory_system.entity.Supplier;
import ru.ivanov.diplom.inventory_system.repository.*;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceService {
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final DepartmentRepository departmentRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository;
    private final WriteOffReasonRepository writeOffReasonRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getCategories() {
        return equipmentCategoryRepository.findAll()
                .stream()
                .map(category -> new ReferenceItemResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getStatuses() {
        return equipmentStatusRepository.findAll()
                .stream()
                .map(status -> new ReferenceItemResponse(
                        status.getId(),
                        status.getName(),
                        status.getDescription()
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(department -> new ReferenceItemResponse(
                        department.getId(),
                        department.getName(),
                        department.getDescription()
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceLocationResponse> getLocations() {
        return storageLocationRepository.findAll()
                .stream()
                .map(this::toLocationResponse)
                .sorted(Comparator.comparing(ReferenceLocationResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceEmployeeResponse> getEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toEmployeeResponse)
                .sorted(Comparator.comparing(ReferenceEmployeeResponse::fullName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceSupplierResponse> getSuppliers() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toSupplierResponse)
                .sorted(Comparator.comparing(ReferenceSupplierResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getWriteOffReasons() {
        return writeOffReasonRepository.findAll()
                .stream()
                .map(reason -> new ReferenceItemResponse(
                        reason.getId(),
                        reason.getName(),
                        reason.getDescription()
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getDocumentTypes() {
        return documentTypeRepository.findAll()
                .stream()
                .map(type -> new ReferenceItemResponse(
                        type.getId(),
                        type.getName(),
                        null
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReferenceItemResponse> getPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(permission -> new ReferenceItemResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getDescription()
                ))
                .sorted(Comparator.comparing(ReferenceItemResponse::name))
                .toList();
    }

    private ReferenceLocationResponse toLocationResponse(StorageLocation location) {
        Department department = location.getDepartment();

        return new ReferenceLocationResponse(
                location.getId(),
                location.getName(),
                location.getBuilding(),
                location.getRoom(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null
        );
    }

    private ReferenceEmployeeResponse toEmployeeResponse(Employee employee) {
        Department department = employee.getDepartment();

        return new ReferenceEmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getPosition(),
                employee.getPhone(),
                employee.getEmail(),
                department != null ? department.getId() : null,
                department != null ? department.getName() : null
        );
    }

    private ReferenceSupplierResponse toSupplierResponse(Supplier supplier) {
        return new ReferenceSupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getInn(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail()
        );
    }
}
