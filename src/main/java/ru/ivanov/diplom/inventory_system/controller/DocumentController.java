package ru.ivanov.diplom.inventory_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentShortResponse;
import ru.ivanov.diplom.inventory_system.service.DocumentPdfService;
import ru.ivanov.diplom.inventory_system.service.DocumentService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentPdfService documentPdfService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DOCUMENT_VIEW')")
    @GetMapping
    public List<DocumentShortResponse> getAllDocuments(
            @RequestParam(required = false) String documentTypeCode,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,

            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String search
    ) {
        return documentService.getAllDocuments(
                documentTypeCode,
                dateFrom,
                dateTo,
                equipmentId,
                search
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DOCUMENT_VIEW')")
    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('DOCUMENT_VIEW')")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        byte[] pdf = documentPdfService.generateDocumentPdf(id);
        String fileName = documentPdfService.buildFileName(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\""
                )
                .body(pdf);
    }
}
