package ru.ivanov.diplom.inventory_system.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentCreateRequest;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentRegistrationResponse;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentRegistrationResponse registerEquipment(
            @Valid @RequestBody EquipmentCreateRequest request) {
        return equipmentService.registerEquipment(request);
    }

    @GetMapping
    public List<EquipmentResponse> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    @GetMapping("/{id}")
    public EquipmentResponse getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id);
    }
}
