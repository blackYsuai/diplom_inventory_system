package ru.ivanov.diplom.inventory_system.mapper;

import org.springframework.stereotype.Component;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentItemResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentShortResponse;
import ru.ivanov.diplom.inventory_system.entity.DocumentItem;
import ru.ivanov.diplom.inventory_system.entity.InventoryDocument;

import java.util.List;

@Component
public class DocumentMapper {

    public DocumentShortResponse toShortResponse(InventoryDocument document, List<DocumentItem> items) {
        if (document == null) {
            return null;
        }

        List<String> equipmentNames = items == null
                ? List.of()
                : items.stream()
                .filter(item -> item.getEquipment() != null)
                .map(item -> item.getEquipment().getInventoryNumber() + " — " + item.getEquipment().getName())
                .toList();

        return new DocumentShortResponse(
                document.getId(),
                document.getDocumentNumber(),
                document.getDocumentDate(),

                document.getDocumentType() != null ? document.getDocumentType().getCode() : null,
                document.getDocumentType() != null ? document.getDocumentType().getName() : null,

                document.getComment(),

                document.getCreatedByUser() != null ? document.getCreatedByUser().getUsername() : null,

                equipmentNames
        );
    }

    public DocumentResponse toResponse(InventoryDocument document, List<DocumentItem> items) {
        if (document == null) {
            return null;
        }

        List<DocumentItemResponse> itemResponses = items == null
                ? List.of()
                : items.stream()
                .map(this::toItemResponse)
                .toList();

        return new DocumentResponse(
                document.getId(),
                document.getDocumentNumber(),
                document.getDocumentDate(),

                document.getDocumentType() != null ? document.getDocumentType().getCode() : null,
                document.getDocumentType() != null ? document.getDocumentType().getName() : null,

                document.getComment(),

                document.getCreatedByUser() != null ? document.getCreatedByUser().getId() : null,
                document.getCreatedByUser() != null ? document.getCreatedByUser().getUsername() : null,

                itemResponses
        );
    }

    public DocumentItemResponse toItemResponse(DocumentItem item) {
        if (item == null) {
            return null;
        }

        return new DocumentItemResponse(
                item.getId(),

                item.getEquipment() != null ? item.getEquipment().getId() : null,
                item.getEquipment() != null ? item.getEquipment().getInventoryNumber() : null,
                item.getEquipment() != null ? item.getEquipment().getName() : null,

                item.getFromLocation() != null ? item.getFromLocation().getId() : null,
                item.getFromLocation() != null ? item.getFromLocation().getName() : null,

                item.getToLocation() != null ? item.getToLocation().getId() : null,
                item.getToLocation() != null ? item.getToLocation().getName() : null,

                item.getFromEmployee() != null ? item.getFromEmployee().getId() : null,
                item.getFromEmployee() != null ? item.getFromEmployee().getFullName() : null,

                item.getToEmployee() != null ? item.getToEmployee().getId() : null,
                item.getToEmployee() != null ? item.getToEmployee().getFullName() : null,

                item.getNote()
        );
    }
}
