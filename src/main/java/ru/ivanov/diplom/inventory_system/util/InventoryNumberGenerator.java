package ru.ivanov.diplom.inventory_system.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ivanov.diplom.inventory_system.repository.EquipmentRepository;

@Component
@RequiredArgsConstructor
public class InventoryNumberGenerator {

    private static final long MAX_SEQUENCE_VALUE = 999_999_999_999L;

    private final EquipmentRepository equipmentRepository;

    public String generate() {
        Long sequence = equipmentRepository.getNextInventoryNumberSequence();

        while (sequence <= MAX_SEQUENCE_VALUE) {
            String inventoryNumber = format(sequence);

            if (!equipmentRepository.existsByInventoryNumber(inventoryNumber)) {
                return inventoryNumber;
            }

            sequence = equipmentRepository.getNextInventoryNumberSequence();
        }

        throw new IllegalStateException("Достигнут лимит инвентарных номеров");
    }

    private String format(Long sequence) {
        String digits = String.format("%012d", sequence);

        return "INV-"
                + digits.substring(0, 4)
                + "-"
                + digits.substring(4, 8)
                + "-"
                + digits.substring(8, 12);
    }
}