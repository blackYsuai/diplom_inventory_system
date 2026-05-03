package ru.ivanov.diplom.inventory_system.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.equipment.*;
import ru.ivanov.diplom.inventory_system.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_CREATE')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentRegistrationResponse registerEquipment(
            @Valid @RequestBody EquipmentCreateRequest request) {
        return equipmentService.registerEquipment(request);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_VIEW')")
    @GetMapping
    public List<EquipmentResponse> getAllEquipment(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long responsibleEmployeeId,
            @RequestParam(required = false) String search
    ) {
        return equipmentService.getAllEquipment(
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId,
                search
        );
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_VIEW')")
    @GetMapping("/{id}/history")
    public List<EquipmentHistoryItemResponse> getEquipmentHistory(@PathVariable Long id) {
        return equipmentService.getEquipmentHistory(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_VIEW')")
    @GetMapping("/{id}")
    public EquipmentResponse getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_UPDATE')")
    @PutMapping("/{id}")
    public EquipmentResponse updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentUpdateRequest request
    ) {
        return equipmentService.updateEquipment(id, request);
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('EQUIPMENT_UPDATE')")
    @PatchMapping("/{id}/status")
    public EquipmentResponse changeEquipmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentStatusChangeRequest request
    ) {
        return equipmentService.changeEquipmentStatus(id, request);
    }
}
