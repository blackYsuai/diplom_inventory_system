package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.dto.operation.EquipmentOperationResponse;
import ru.ivanov.diplom.inventory_system.dto.operation.MoveEquipmentRequest;
import ru.ivanov.diplom.inventory_system.dto.operation.WriteOffEquipmentRequest;
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
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InventoryOperationService {
    private final EquipmentRepository equipmentRepository;
    private final StorageLocationRepository storageLocationRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;
    private final EquipmentStatusRepository equipmentStatusRepository;
    private final WriteOffReasonRepository writeOffReasonRepository;

    private final CurrentUserService currentUserService;
    private final DocumentNumberGenerator documentNumberGenerator;
    private final EquipmentMapper equipmentMapper;
    private final DocumentMapper documentMapper;

    @Transactional
    public EquipmentOperationResponse moveEquipment(MoveEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + request.equipmentId() + " не найдено"
                ));

        validateEquipmentCanBeMoved(equipment);

        StorageLocation fromLocation = equipment.getLocation();
        Employee fromEmployee = equipment.getResponsibleEmployee();

        StorageLocation toLocation = resolveTargetLocation(request, fromLocation);
        Employee toEmployee = resolveTargetEmployee(request, fromEmployee);

        validateMovementHasChanges(fromLocation, toLocation, fromEmployee, toEmployee);

        equipment.setLocation(toLocation);
        equipment.setResponsibleEmployee(toEmployee);

        Equipment savedEquipment = equipmentRepository.save(equipment);

        InventoryDocument movementDocument = createMovementDocument(savedEquipment);
        DocumentItem documentItem = createMovementDocumentItem(
                movementDocument,
                savedEquipment,
                fromLocation,
                toLocation,
                fromEmployee,
                toEmployee,
                request.note()
        );

        EquipmentResponse equipmentResponse = equipmentMapper.toResponse(savedEquipment);
        DocumentResponse documentResponse = documentMapper.toResponse(
                movementDocument,
                List.of(documentItem)
        );

        return new EquipmentOperationResponse(equipmentResponse, documentResponse);
    }

    @Transactional
    public EquipmentOperationResponse writeOffEquipment(WriteOffEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Оборудование с id " + request.equipmentId() + " не найдено"
                ));

        validateEquipmentCanBeWrittenOff(equipment);

        WriteOffReason writeOffReason = writeOffReasonRepository.findById(request.writeOffReasonId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Причина списания с id " + request.writeOffReasonId() + " не найдена"
                ));

        EquipmentStatus writtenOffStatus = equipmentStatusRepository
                .findByName(EquipmentStatusName.WRITTEN_OFF.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Статус оборудования 'Списано' не найден"
                ));

        StorageLocation currentLocation = equipment.getLocation();
        Employee currentEmployee = equipment.getResponsibleEmployee();

        equipment.setStatus(writtenOffStatus);

        Equipment savedEquipment = equipmentRepository.save(equipment);

        InventoryDocument writeOffDocument = createWriteOffDocument(savedEquipment);
        DocumentItem documentItem = createWriteOffDocumentItem(
                writeOffDocument,
                savedEquipment,
                currentLocation,
                currentEmployee,
                writeOffReason,
                request.note()
        );

        EquipmentResponse equipmentResponse = equipmentMapper.toResponse(savedEquipment);
        DocumentResponse documentResponse = documentMapper.toResponse(
                writeOffDocument,
                List.of(documentItem)
        );

        return new EquipmentOperationResponse(equipmentResponse, documentResponse);
    }

    private void validateEquipmentCanBeWrittenOff(Equipment equipment) {
        if (equipment.getStatus() != null
                && EquipmentStatusName.WRITTEN_OFF.getName().equals(equipment.getStatus().getName())) {
            throw new BadRequestException("Оборудование уже списано");
        }
    }

    private InventoryDocument createWriteOffDocument(Equipment equipment) {
        AppUser currentUser = currentUserService.getCurrentUser();

        DocumentType writeOffType = documentTypeRepository
                .findByCode(DocumentTypeCode.WRITE_OFF.getCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тип документа WRITE_OFF не найден"
                ));

        InventoryDocument document = InventoryDocument.builder()
                .documentNumber(documentNumberGenerator.generate(writeOffType.getCode()))
                .documentDate(LocalDate.now())
                .comment("Списание оборудования: " + equipment.getName())
                .documentType(writeOffType)
                .createdByUser(currentUser)
                .build();

        return inventoryDocumentRepository.save(document);
    }

    private DocumentItem createWriteOffDocumentItem(
            InventoryDocument document,
            Equipment equipment,
            StorageLocation currentLocation,
            Employee currentEmployee,
            WriteOffReason writeOffReason,
            String note
    ) {
        DocumentItem item = DocumentItem.builder()
                .document(document)
                .equipment(equipment)
                .fromLocation(currentLocation)
                .toLocation(null)
                .fromEmployee(currentEmployee)
                .toEmployee(null)
                .note(note)
                .build();

        item.getWriteOffReasons().add(writeOffReason);

        return documentItemRepository.save(item);
    }

    private void validateEquipmentCanBeMoved(Equipment equipment) {
        if (equipment.getStatus() != null
                && EquipmentStatusName.WRITTEN_OFF.getName().equals(equipment.getStatus().getName())) {
            throw new BadRequestException("Списанное оборудование нельзя переместить");
        }
    }

    private StorageLocation resolveTargetLocation(
            MoveEquipmentRequest request,
            StorageLocation currentLocation
    ) {
        if (request.toLocationId() == null) {
            return currentLocation;
        }

        return storageLocationRepository.findById(request.toLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Новое местоположение с id " + request.toLocationId() + " не найдено"
                ));
    }

    private Employee resolveTargetEmployee(
            MoveEquipmentRequest request,
            Employee currentEmployee
    ) {
        if (request.toEmployeeId() == null) {
            return currentEmployee;
        }

        return employeeRepository.findById(request.toEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Новый ответственный сотрудник с id " + request.toEmployeeId() + " не найден"
                ));
    }

    private void validateMovementHasChanges(
            StorageLocation fromLocation,
            StorageLocation toLocation,
            Employee fromEmployee,
            Employee toEmployee
    ) {
        boolean locationChanged = !Objects.equals(
                fromLocation != null ? fromLocation.getId() : null,
                toLocation != null ? toLocation.getId() : null
        );

        boolean employeeChanged = !Objects.equals(
                fromEmployee != null ? fromEmployee.getId() : null,
                toEmployee != null ? toEmployee.getId() : null
        );

        if (!locationChanged && !employeeChanged) {
            throw new BadRequestException(
                    "Для перемещения необходимо указать новое местоположение или нового ответственного сотрудника"
            );
        }
    }

    private InventoryDocument createMovementDocument(Equipment equipment) {
        AppUser currentUser = currentUserService.getCurrentUser();

        DocumentType movementType = documentTypeRepository
                .findByCode(DocumentTypeCode.MOVEMENT.getCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Тип документа MOVEMENT не найден"
                ));

        InventoryDocument document = InventoryDocument.builder()
                .documentNumber(documentNumberGenerator.generate(movementType.getCode()))
                .documentDate(LocalDate.now())
                .comment("Перемещение оборудования: " + equipment.getName())
                .documentType(movementType)
                .createdByUser(currentUser)
                .build();

        return inventoryDocumentRepository.save(document);
    }

    private DocumentItem createMovementDocumentItem(
            InventoryDocument document,
            Equipment equipment,
            StorageLocation fromLocation,
            StorageLocation toLocation,
            Employee fromEmployee,
            Employee toEmployee,
            String note
    ) {
        DocumentItem item = DocumentItem.builder()
                .document(document)
                .equipment(equipment)
                .fromLocation(fromLocation)
                .toLocation(toLocation)
                .fromEmployee(fromEmployee)
                .toEmployee(toEmployee)
                .note(note)
                .build();

        return documentItemRepository.save(item);
    }
}
