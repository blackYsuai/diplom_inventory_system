package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminLocationRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminReferenceItemRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminSupplierRequest;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceItemResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceLocationResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceSupplierResponse;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.repository.*;

@Service
@RequiredArgsConstructor
public class AdminReferenceService {
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final DepartmentRepository departmentRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final SupplierRepository supplierRepository;
    private final WriteOffReasonRepository writeOffReasonRepository;


    @Transactional
    public ReferenceItemResponse createCategory(AdminReferenceItemRequest request) {
        String name = normalizeRequired(request.name());

        if (equipmentCategoryRepository.existsByName(name)) {
            throw new BadRequestException("Категория оборудования с названием " + name + " уже существует");
        }

        EquipmentCategory category = EquipmentCategory.builder()
                .name(name)
                .description(normalizeBlank(request.description()))
                .build();

        EquipmentCategory saved = equipmentCategoryRepository.save(category);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public ReferenceItemResponse updateCategory(Long id, AdminReferenceItemRequest request) {
        EquipmentCategory category = equipmentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Категория оборудования с id " + id + " не найдена"
                ));

        String name = normalizeRequired(request.name());

        if (equipmentCategoryRepository.existsByNameAndIdNot(name, id)) {
            throw new BadRequestException("Категория оборудования с названием " + name + " уже существует");
        }

        category.setName(name);
        category.setDescription(normalizeBlank(request.description()));

        EquipmentCategory saved = equipmentCategoryRepository.save(category);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
    }


    @Transactional
    public ReferenceItemResponse createDepartment(AdminReferenceItemRequest request) {
        String name = normalizeRequired(request.name());

        if (departmentRepository.existsByName(name)) {
            throw new BadRequestException("Подразделение с названием " + name + " уже существует");
        }

        Department department = Department.builder()
                .name(name)
                .description(normalizeBlank(request.description()))
                .build();

        Department saved = departmentRepository.save(department);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public ReferenceItemResponse updateDepartment(Long id, AdminReferenceItemRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Подразделение с id " + id + " не найдено"
                ));

        String name = normalizeRequired(request.name());

        if (departmentRepository.existsByNameAndIdNot(name, id)) {
            throw new BadRequestException("Подразделение с названием " + name + " уже существует");
        }

        department.setName(name);
        department.setDescription(normalizeBlank(request.description()));

        Department saved = departmentRepository.save(department);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
    }


    @Transactional
    public ReferenceLocationResponse createLocation(AdminLocationRequest request) {
        String name = normalizeRequired(request.name());

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Подразделение с id " + request.departmentId() + " не найдено"
                ));

        if (storageLocationRepository.existsByNameAndDepartmentId(name, department.getId())) {
            throw new BadRequestException(
                    "Местоположение с названием " + name + " уже существует в выбранном подразделении"
            );
        }

        StorageLocation location = StorageLocation.builder()
                .name(name)
                .building(normalizeBlank(request.building()))
                .room(normalizeBlank(request.room()))
                .department(department)
                .build();

        StorageLocation saved = storageLocationRepository.save(location);

        return toLocationResponse(saved);
    }

    @Transactional
    public ReferenceLocationResponse updateLocation(Long id, AdminLocationRequest request) {
        StorageLocation location = storageLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Местоположение с id " + id + " не найдено"
                ));

        String name = normalizeRequired(request.name());

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Подразделение с id " + request.departmentId() + " не найдено"
                ));

        if (storageLocationRepository.existsByNameAndDepartmentIdAndIdNot(
                name,
                department.getId(),
                id
        )) {
            throw new BadRequestException(
                    "Местоположение с названием " + name + " уже существует в выбранном подразделении"
            );
        }

        location.setName(name);
        location.setBuilding(normalizeBlank(request.building()));
        location.setRoom(normalizeBlank(request.room()));
        location.setDepartment(department);

        StorageLocation saved = storageLocationRepository.save(location);

        return toLocationResponse(saved);
    }

    @Transactional
    public ReferenceSupplierResponse createSupplier(AdminSupplierRequest request) {
        String name = normalizeRequired(request.name());
        String inn = normalizeBlank(request.inn());

        if (inn != null && supplierRepository.existsByInn(inn)) {
            throw new BadRequestException("Поставщик с ИНН " + inn + " уже существует");
        }

        Supplier supplier = Supplier.builder()
                .name(name)
                .inn(inn)
                .contactPerson(normalizeBlank(request.contactPerson()))
                .phone(normalizeBlank(request.phone()))
                .email(normalizeBlank(request.email()))
                .build();

        Supplier saved = supplierRepository.save(supplier);

        return toSupplierResponse(saved);
    }

    @Transactional
    public ReferenceSupplierResponse updateSupplier(Long id, AdminSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Поставщик с id " + id + " не найден"
                ));

        String name = normalizeRequired(request.name());
        String inn = normalizeBlank(request.inn());

        if (inn != null && supplierRepository.existsByInnAndIdNot(inn, id)) {
            throw new BadRequestException("Поставщик с ИНН " + inn + " уже существует");
        }

        supplier.setName(name);
        supplier.setInn(inn);
        supplier.setContactPerson(normalizeBlank(request.contactPerson()));
        supplier.setPhone(normalizeBlank(request.phone()));
        supplier.setEmail(normalizeBlank(request.email()));

        Supplier saved = supplierRepository.save(supplier);

        return toSupplierResponse(saved);
    }


    @Transactional
    public ReferenceItemResponse createWriteOffReason(AdminReferenceItemRequest request) {
        String name = normalizeRequired(request.name());

        if (writeOffReasonRepository.existsByName(name)) {
            throw new BadRequestException("Причина списания с названием " + name + " уже существует");
        }

        WriteOffReason reason = WriteOffReason.builder()
                .name(name)
                .description(normalizeBlank(request.description()))
                .build();

        WriteOffReason saved = writeOffReasonRepository.save(reason);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public ReferenceItemResponse updateWriteOffReason(Long id, AdminReferenceItemRequest request) {
        WriteOffReason reason = writeOffReasonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Причина списания с id " + id + " не найдена"
                ));

        String name = normalizeRequired(request.name());

        if (writeOffReasonRepository.existsByNameAndIdNot(name, id)) {
            throw new BadRequestException("Причина списания с названием " + name + " уже существует");
        }

        reason.setName(name);
        reason.setDescription(normalizeBlank(request.description()));

        WriteOffReason saved = writeOffReasonRepository.save(reason);

        return new ReferenceItemResponse(saved.getId(), saved.getName());
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

    private String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Наименование не может быть пустым");
        }

        return value.trim();
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
