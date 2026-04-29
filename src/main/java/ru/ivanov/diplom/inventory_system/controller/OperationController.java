package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ivanov.diplom.inventory_system.dto.operation.EquipmentOperationResponse;
import ru.ivanov.diplom.inventory_system.dto.operation.MoveEquipmentRequest;
import ru.ivanov.diplom.inventory_system.service.InventoryOperationService;
import ru.ivanov.diplom.inventory_system.dto.operation.WriteOffEquipmentRequest;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
public class OperationController {
    private final InventoryOperationService inventoryOperationService;

    @PostMapping("/move")
    public EquipmentOperationResponse moveEquipment(
            @Valid @RequestBody MoveEquipmentRequest request
    ) {
        return inventoryOperationService.moveEquipment(request);
    }
    
    @PostMapping("/write-off")
    public EquipmentOperationResponse writeOffEquipment(
            @Valid @RequestBody WriteOffEquipmentRequest request
    ) {
        return inventoryOperationService.writeOffEquipment(request);
    }
}
