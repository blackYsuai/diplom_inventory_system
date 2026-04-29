package ru.ivanov.diplom.inventory_system.mapper;

import org.springframework.stereotype.Component;
import ru.ivanov.diplom.inventory_system.dto.equipment.EquipmentResponse;
import ru.ivanov.diplom.inventory_system.entity.Equipment;

@Component
public class EquipmentMapper {
    public EquipmentResponse toResponse(Equipment equipment) {
        if (equipment == null) {
            return null;
        }
        return new EquipmentResponse(
                equipment.getId(),
                equipment.getInventoryNumber(),
                equipment.getName(),
                equipment.getModel(),
                equipment.getSerialNumber(),
                equipment.getPurchaseDate(),
                equipment.getCommissioningDate(),
                equipment.getInitialCost(),
                equipment.getUsefulLifeMonths(),
                equipment.getDescription(),

                equipment.getCategory() != null ? equipment.getCategory().getId() : null,
                equipment.getCategory() != null ? equipment.getCategory().getName() : null,

                equipment.getStatus() != null ? equipment.getStatus().getId() : null,
                equipment.getStatus() != null ? equipment.getStatus().getName() : null,

                equipment.getLocation() != null ? equipment.getLocation().getId() : null,
                equipment.getLocation() != null ? equipment.getLocation().getName() : null,

                equipment.getResponsibleEmployee() != null ? equipment.getResponsibleEmployee().getId() : null,
                equipment.getResponsibleEmployee() != null ? equipment.getResponsibleEmployee().getFullName() : null
        );
    }
}
