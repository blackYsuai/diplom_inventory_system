package ru.ivanov.diplom.inventory_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentShortResponse;
import ru.ivanov.diplom.inventory_system.entity.DocumentItem;
import ru.ivanov.diplom.inventory_system.entity.InventoryDocument;
import ru.ivanov.diplom.inventory_system.exception.ResourceNotFoundException;
import ru.ivanov.diplom.inventory_system.mapper.DocumentMapper;
import ru.ivanov.diplom.inventory_system.repository.DocumentItemRepository;
import ru.ivanov.diplom.inventory_system.repository.InventoryDocumentRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;
    private final DocumentMapper documentMapper;

    @Transactional(readOnly = true)
    public List<DocumentShortResponse> getAllDocuments(
            String documentTypeCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long equipmentId,
            String search
    ) {
        String normalizedTypeCode = normalizeBlank(documentTypeCode);
        String normalizedSearch = normalizeBlank(search);

        return inventoryDocumentRepository.findAllByOrderByDocumentDateDescIdDesc()
                .stream()
                .filter(document -> normalizedTypeCode == null
                        || document.getDocumentType() != null
                        && normalizedTypeCode.equals(document.getDocumentType().getCode()))
                .filter(document -> dateFrom == null
                        || document.getDocumentDate() != null
                        && !document.getDocumentDate().isBefore(dateFrom))
                .filter(document -> dateTo == null
                        || document.getDocumentDate() != null
                        && !document.getDocumentDate().isAfter(dateTo))
                .map(document -> {
                    List<DocumentItem> items = documentItemRepository.findAllByDocumentId(document.getId());
                    return new DocumentWithItems(document, items);
                })
                .filter(wrapper -> equipmentId == null || containsEquipment(wrapper.items(), equipmentId))
                .filter(wrapper -> normalizedSearch == null || documentMatchesSearch(wrapper, normalizedSearch))
                .map(wrapper -> documentMapper.toShortResponse(wrapper.document(), wrapper.items()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentShortResponse> getAllDocuments() {
        return getAllDocuments(null, null, null, null, null);
    }


    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        InventoryDocument document = inventoryDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Документ с id " + id + " не найден"
                ));

        List<DocumentItem> items = documentItemRepository.findAllByDocumentId(document.getId());

        return documentMapper.toResponse(document, items);
    }
    private boolean containsEquipment(List<DocumentItem> items, Long equipmentId) {
        return items.stream()
                .anyMatch(item -> item.getEquipment() != null
                        && equipmentId.equals(item.getEquipment().getId()));
    }

    private boolean documentMatchesSearch(DocumentWithItems wrapper, String search) {
        InventoryDocument document = wrapper.document();

        if (containsIgnoreCase(document.getDocumentNumber(), search)
                || containsIgnoreCase(document.getComment(), search)) {
            return true;
        }

        return wrapper.items()
                .stream()
                .anyMatch(item -> item.getEquipment() != null
                        && (containsIgnoreCase(item.getEquipment().getInventoryNumber(), search)
                        || containsIgnoreCase(item.getEquipment().getName(), search)
                        || containsIgnoreCase(item.getEquipment().getModel(), search)
                        || containsIgnoreCase(item.getEquipment().getSerialNumber(), search)));
    }

    private boolean containsIgnoreCase(String value, String search) {
        return value != null && value.toLowerCase().contains(search.toLowerCase());
    }

    private String normalizeBlank(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private record DocumentWithItems(
            InventoryDocument document,
            List<DocumentItem> items
    ) {
    }
}