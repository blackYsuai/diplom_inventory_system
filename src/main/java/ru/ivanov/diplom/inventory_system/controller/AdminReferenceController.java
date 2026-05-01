package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminLocationRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminReferenceItemRequest;
import ru.ivanov.diplom.inventory_system.dto.admin.reference.AdminSupplierRequest;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceItemResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceLocationResponse;
import ru.ivanov.diplom.inventory_system.dto.reference.ReferenceSupplierResponse;
import ru.ivanov.diplom.inventory_system.service.AdminReferenceService;

@RestController
@RequestMapping("/api/admin/references")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('REFERENCE_MANAGE')")
public class AdminReferenceController {
    private final AdminReferenceService adminReferenceService;


    @PostMapping("/categories")
    public ReferenceItemResponse createCategory(
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public ReferenceItemResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.updateCategory(id, request);
    }


    @PostMapping("/departments")
    public ReferenceItemResponse createDepartment(
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.createDepartment(request);
    }

    @PutMapping("/departments/{id}")
    public ReferenceItemResponse updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.updateDepartment(id, request);
    }


    @PostMapping("/locations")
    public ReferenceLocationResponse createLocation(
            @Valid @RequestBody AdminLocationRequest request
    ) {
        return adminReferenceService.createLocation(request);
    }

    @PutMapping("/locations/{id}")
    public ReferenceLocationResponse updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody AdminLocationRequest request
    ) {
        return adminReferenceService.updateLocation(id, request);
    }


    @PostMapping("/suppliers")
    public ReferenceSupplierResponse createSupplier(
            @Valid @RequestBody AdminSupplierRequest request
    ) {
        return adminReferenceService.createSupplier(request);
    }

    @PutMapping("/suppliers/{id}")
    public ReferenceSupplierResponse updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody AdminSupplierRequest request
    ) {
        return adminReferenceService.updateSupplier(id, request);
    }


    @PostMapping("/write-off-reasons")
    public ReferenceItemResponse createWriteOffReason(
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.createWriteOffReason(request);
    }

    @PutMapping("/write-off-reasons/{id}")
    public ReferenceItemResponse updateWriteOffReason(
            @PathVariable Long id,
            @Valid @RequestBody AdminReferenceItemRequest request
    ) {
        return adminReferenceService.updateWriteOffReason(id, request);
    }
}
