package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentResponse;
import ru.ivanov.diplom.inventory_system.dto.document.DocumentShortResponse;
import ru.ivanov.diplom.inventory_system.dto.employee.ChangeOwnPasswordRequest;
import ru.ivanov.diplom.inventory_system.dto.employee.ChangeOwnPasswordResponse;
import ru.ivanov.diplom.inventory_system.dto.employee.EmployeeDashboardResponse;
import ru.ivanov.diplom.inventory_system.dto.employee.EmployeeProfileResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.service.DocumentPdfService;
import ru.ivanov.diplom.inventory_system.service.EmployeeCabinetService;

import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeCabinetController {
    private final EmployeeCabinetService employeeCabinetService;
    private final DocumentPdfService documentPdfService;

    @GetMapping("/dashboard")
    public EmployeeDashboardResponse getDashboard() {
        return employeeCabinetService.getDashboard();
    }

    @GetMapping("/profile")
    public EmployeeProfileResponse getProfile() {
        return employeeCabinetService.getProfile();
    }

    @GetMapping("/equipment")
    public List<EquipmentResponse> getMyEquipment() {
        return employeeCabinetService.getMyEquipment();
    }

    @GetMapping("/documents")
    public List<DocumentShortResponse> getMyDocuments() {
        return employeeCabinetService.getMyDocuments();
    }

    @GetMapping("/documents/{id}")
    public DocumentResponse getMyDocumentById(@PathVariable Long id) {
        return employeeCabinetService.getMyDocumentById(id);
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<byte[]> downloadMyDocument(@PathVariable Long id) {
        employeeCabinetService.checkDocumentAccess(id);

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

    @PatchMapping("/change-password")
    public ChangeOwnPasswordResponse changeOwnPassword(
            @Valid @RequestBody ChangeOwnPasswordRequest request
    ) {
        return employeeCabinetService.changeOwnPassword(request);
    }
}
