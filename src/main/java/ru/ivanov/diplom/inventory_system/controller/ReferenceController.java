package ru.ivanov.diplom.inventory_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceEmployeeResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceItemResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceLocationResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceSupplierResponse;
import ru.ivanov.diplom.inventory_system.service.ReferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/references")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ReferenceController {
    private final ReferenceService referenceService;

    @GetMapping("/categories")
    public List<ReferenceItemResponse> getCategories() {
        return referenceService.getCategories();
    }

    @GetMapping("/statuses")
    public List<ReferenceItemResponse> getStatuses() {
        return referenceService.getStatuses();
    }

    @GetMapping("/departments")
    public List<ReferenceItemResponse> getDepartments() {
        return referenceService.getDepartments();
    }

    @GetMapping("/locations")
    public List<ReferenceLocationResponse> getLocations() {
        return referenceService.getLocations();
    }

    @GetMapping("/employees")
    public List<ReferenceEmployeeResponse> getEmployees() {
        return referenceService.getEmployees();
    }

    @GetMapping("/suppliers")
    public List<ReferenceSupplierResponse> getSuppliers() {
        return referenceService.getSuppliers();
    }

    @GetMapping("/write-off-reasons")
    public List<ReferenceItemResponse> getWriteOffReasons() {
        return referenceService.getWriteOffReasons();
    }

    @GetMapping("/document-types")
    public List<ReferenceItemResponse> getDocumentTypes() {
        return referenceService.getDocumentTypes();
    }

    @GetMapping("/permissions")
    public List<ReferenceItemResponse> getPermissions() {
        return referenceService.getPermissions();
    }
}
