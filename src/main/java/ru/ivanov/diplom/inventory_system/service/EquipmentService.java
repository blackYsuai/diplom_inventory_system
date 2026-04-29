package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentCreateRequest;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentRegistrationResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.entity.*;
import ru.ivanov.diplom.inventory_system.entity.enums.DocumentTypeCode;
import ru.ivanov.diplom.inventory_system.exception.BadRequestException;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.mapper.DocumentMapper;
import ru.ivanov.diplom.inventory_system.mapper.EquipmentMapper;
import ru.ivanov.diplom.inventory_system.repository.*;
import ru.ivanov.diplom.inventory_system.util.DocumentNumberGenerator;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll()
                .stream()
                .map(equipmentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + id + " не найдено"
                ));

        return equipmentMapper.toResponse(equipment);
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
}