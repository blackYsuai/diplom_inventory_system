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

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final InventoryDocumentRepository inventoryDocumentRepository;
    private final DocumentItemRepository documentItemRepository;
    private final DocumentMapper documentMapper;

    @Transactional(readOnly = true)
    public List<DocumentShortResponse> getAllDocuments() {
        return inventoryDocumentRepository.findAllByOrderByDocumentDateDescIdDesc()
                .stream()
                .map(document -> {
                    List<DocumentItem> items = documentItemRepository.findAllByDocumentId(document.getId());
                    return documentMapper.toShortResponse(document, items);
                })
                .toList();
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
}