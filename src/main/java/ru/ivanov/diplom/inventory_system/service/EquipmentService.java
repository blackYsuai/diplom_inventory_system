package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.*;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.entity.enums.DocumentTypeCode;
import ru.ivanov.diplom.inventory_system.entity.enums.EquipmentStatusName;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.mapper.DocumentMapper;
import ru.ivanov.diplom.inventory_system.mapper.EquipmentMapper;
import ru.ivanov.diplom.inventory_system.repository.*;
import ru.ivanov.diplom.inventory_system.util.DocumentNumberGenerator;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository equipmentCategoryRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final EmployeeRepository employeeRepository;
    private final SupplierRepository supplierRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;

    private final CurrentUserService currentUserService;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final EquipmentMapper equipmentMapper;
    private final DocumentMapper documentMapper;

    @Transactional
    public EquipmentRegistrationResponse registerEquipment(EquipmentCreateRequest request) {
        validateEquipmentCreateRequest(request);

        EquipmentCategory category = equipmentCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Категория оборудования с id " + request.categoryId() + " не найдена"
                ));

        EquipmentStatus status = equipmentStatusRepository.findById(request.statusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Статус оборудования с id " + request.statusId() + " не найден"
                ));

        StorageLocation location = storageLocationRepository.findById(request.locationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Местоположение с id " + request.locationId() + " не найдено"
                ));

        validateInitialStatus(status);

        Employee responsibleEmployee = employeeRepository.findById(request.responsibleEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ответственный сотрудник с id " + request.responsibleEmployeeId() + " не найден"
                ));

        Equipment equipment = Equipment.builder()
                .inventoryNumber(request.inventoryNumber())
                .name(request.name())
                .model(request.model())
                .serialNumber(normalizeBlank(request.serialNumber()))
                .purchaseDate(request.purchaseDate())
                .commissioningDate(request.commissioningDate())
                .initialCost(request.initialCost())
                .usefulLifeMonths(request.usefulLifeMonths())
                .description(request.description())
                .category(category)
                .status(status)
                .location(location)
                .responsibleEmployee(responsibleEmployee)
                .build();

        Equipment savedEquipment = equipmentRepository.save(equipment);

        InventoryDocument receiptDocument = createReceiptDocument(request);
        DocumentItem documentItem = createReceiptDocumentItem(
                receiptDocument,
                savedEquipment,
                location,
                responsibleEmployee
        );

        EquipmentResponse equipmentResponse = equipmentMapper.toResponse(savedEquipment);
        DocumentResponse documentResponse = documentMapper.toResponse(
                receiptDocument,
                List.of(documentItem)
        );

        return new EquipmentRegistrationResponse(equipmentResponse, documentResponse);
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipment(
            Long statusId,
            Long categoryId,
            Long locationId,
            Long responsibleEmployeeId,
            String search
    ) {
        String normalizedSearch = normalizeBlank(search);

        return equipmentRepository.findAll()
                .stream()
                .filter(equipment -> statusId == null
                        || equipment.getStatus() != null
                        && Objects.equals(equipment.getStatus().getId(), statusId))
                .filter(equipment -> categoryId == null
                        || equipment.getCategory() != null
                        && Objects.equals(equipment.getCategory().getId(), categoryId))
                .filter(equipment -> locationId == null
                        || equipment.getLocation() != null
                        && Objects.equals(equipment.getLocation().getId(), locationId))
                .filter(equipment -> responsibleEmployeeId == null
                        || equipment.getResponsibleEmployee() != null
                        && Objects.equals(equipment.getResponsibleEmployee().getId(), responsibleEmployeeId))
                .filter(equipment -> normalizedSearch == null
                        || containsIgnoreCase(equipment.getInventoryNumber(), normalizedSearch)
                        || containsIgnoreCase(equipment.getName(), normalizedSearch)
                        || containsIgnoreCase(equipment.getModel(), normalizedSearch)
                        || containsIgnoreCase(equipment.getSerialNumber(), normalizedSearch))
                .map(equipmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getAllEquipment() {
        return getAllEquipment(null, null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<EquipmentHistoryItemResponse> getEquipmentHistory(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException(
                    "Оборудование с id " + equipmentId + " не найдено"
            );
        }

        return documentItemRepository.findAllByEquipmentIdWithDocumentDetails(equipmentId)
                .stream()
                .map(this::toHistoryItemResponse)
                .toList();
    }

    private EquipmentHistoryItemResponse toHistoryItemResponse(DocumentItem item) {
        InventoryDocument document = item.getDocument();

        List<String> writeOffReasons = item.getWriteOffReasons() == null
                ? List.of()
                : item.getWriteOffReasons()
                .stream()
                .map(WriteOffReason::getName)
                .toList();

        return new EquipmentHistoryItemResponse(
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

                getLocationName(item.getFromLocation()),
                getLocationName(item.getToLocation()),

                item.getFromEmployee() != null ? item.getFromEmployee().getFullName() : null,
                item.getToEmployee() != null ? item.getToEmployee().getFullName() : null,

                writeOffReasons,
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

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search.toLowerCase());
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + id + " не найдено"
                ));

        return equipmentMapper.toResponse(equipment);
    }

    @Transactional
    public EquipmentResponse updateEquipment(Long id, EquipmentUpdateRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + id + " не найдено"
                ));

        validateEquipmentUpdateRequest(id, request);

        if (request.inventoryNumber() != null && !request.inventoryNumber().isBlank()) {
            equipment.setInventoryNumber(request.inventoryNumber().trim());
        }

        if (request.name() != null && !request.name().isBlank()) {
            equipment.setName(request.name().trim());
        }

        if (request.model() != null) {
            equipment.setModel(normalizeBlank(request.model()));
        }

        if (request.serialNumber() != null) {
            equipment.setSerialNumber(normalizeBlank(request.serialNumber()));
        }

        if (request.purchaseDate() != null) {
            equipment.setPurchaseDate(request.purchaseDate());
        }

        if (request.commissioningDate() != null) {
            equipment.setCommissioningDate(request.commissioningDate());
        }

        if (request.initialCost() != null) {
            equipment.setInitialCost(request.initialCost());
        }

        if (request.usefulLifeMonths() != null) {
            equipment.setUsefulLifeMonths(request.usefulLifeMonths());
        }

        if (request.description() != null) {
            equipment.setDescription(request.description());
        }

        if (request.categoryId() != null) {
            EquipmentCategory category = equipmentCategoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Категория оборудования с id " + request.categoryId() + " не найдена"
                    ));

            equipment.setCategory(category);
        }

        Equipment savedEquipment = equipmentRepository.save(equipment);

        return equipmentMapper.toResponse(savedEquipment);
    }

    @Transactional
    public EquipmentResponse changeEquipmentStatus(Long id, EquipmentStatusChangeRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + id + " не найдено"
                ));

        if (equipment.getStatus() != null
                && EquipmentStatusName.WRITTEN_OFF.getName().equals(equipment.getStatus().getName())) {
            throw new BadRequestException("Нельзя изменить статус списанного оборудования");
        }

        EquipmentStatus newStatus = equipmentStatusRepository.findById(request.statusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Статус оборудования с id " + request.statusId() + " не найден"
                ));

        if (EquipmentStatusName.WRITTEN_OFF.getName().equals(newStatus.getName())) {
            throw new BadRequestException("Для списания оборудования используйте операцию списания");
        }

        if (equipment.getStatus() != null
                && Objects.equals(equipment.getStatus().getId(), newStatus.getId())) {
            throw new BadRequestException("У оборудования уже установлен выбранный статус");
        }

        equipment.setStatus(newStatus);

        Equipment savedEquipment = equipmentRepository.save(equipment);

        return equipmentMapper.toResponse(savedEquipment);
    }

    private void validateEquipmentUpdateRequest(Long equipmentId, EquipmentUpdateRequest request) {
        if (request.inventoryNumber() != null && !request.inventoryNumber().isBlank()) {
            String inventoryNumber = request.inventoryNumber().trim();

            if (equipmentRepository.existsByInventoryNumberAndIdNot(inventoryNumber, equipmentId)) {
                throw new BadRequestException(
                        "Оборудование с инвентарным номером "
                                + inventoryNumber
                                + " уже существует"
                );
            }
        }

        if (request.serialNumber() != null && !request.serialNumber().isBlank()) {
            String serialNumber = request.serialNumber().trim();

            if (equipmentRepository.existsBySerialNumberAndIdNot(serialNumber, equipmentId)) {
                throw new BadRequestException(
                        "Оборудование с серийным номером "
                                + serialNumber
                                + " уже существует"
                );
            }
        }

        if (request.purchaseDate() != null
                && request.commissioningDate() != null
                && request.commissioningDate().isBefore(request.purchaseDate())) {
            throw new BadRequestException(
                    "Дата ввода в эксплуатацию не может быть раньше даты приобретения"
            );
        }
    }

    private void validateEquipmentCreateRequest(EquipmentCreateRequest request) {
        if (equipmentRepository.existsByInventoryNumber(request.inventoryNumber())) {
            throw new BadRequestException(
                    "Оборудование с инвентарным номером "
                            + request.inventoryNumber()
                            + " уже существует"
            );
        }

        String serialNumber = normalizeBlank(request.serialNumber());

        if (serialNumber != null && equipmentRepository.existsBySerialNumber(serialNumber)) {
            throw new BadRequestException(
                    "Оборудование с серийным номером "
                            + serialNumber
                            + " уже существует"
            );
        }

        if (request.purchaseDate() != null
                && request.commissioningDate() != null
                && request.commissioningDate().isBefore(request.purchaseDate())) {
            throw new BadRequestException(
                    "Дата ввода в эксплуатацию не может быть раньше даты приобретения"
            );
        }
    }

    private InventoryDocument createReceiptDocument(EquipmentCreateRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser();

        DocumentType receiptType = documentTypeRepository
                .findByCode(DocumentTypeCode.RECEIPT.getCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тип документа RECEIPT не найден"
                ));

        Set<Supplier> suppliers = new HashSet<>();

        if (request.supplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Поставщик с id " + request.supplierId() + " не найден"
                    ));

            suppliers.add(supplier);
        }

        InventoryDocument document = InventoryDocument.builder()
                .documentNumber(documentNumberGenerator.generate(receiptType.getCode()))
                .documentDate(LocalDate.now())
                .comment("Поступление оборудования: " + request.name())
                .documentType(receiptType)
                .createdByUser(currentUser)
                .suppliers(suppliers)
                .build();

        return inventoryDocumentRepository.save(document);
    }

    private DocumentItem createReceiptDocumentItem(
            InventoryDocument document,
            Equipment equipment,
            StorageLocation location,
            Employee responsibleEmployee
    ) {
        DocumentItem item = DocumentItem.builder()
                .document(document)
                .equipment(equipment)
                .fromLocation(null)
                .toLocation(location)
                .fromEmployee(null)
                .toEmployee(responsibleEmployee)
                .note("Первичная регистрация оборудования")
                .build();

        return documentItemRepository.save(item);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void validateInitialStatus(EquipmentStatus status) {
        if (!EquipmentStatusName.IN_USE.getName().equals(status.getName())) {
            throw new BadRequestException("При регистрации оборудования начальный статус должен быть 'В эксплуатации'");
        }
    }
}