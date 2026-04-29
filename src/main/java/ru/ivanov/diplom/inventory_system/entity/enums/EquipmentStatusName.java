package ru.ivanov.diplom.inventory_system.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


public enum EquipmentStatusName {
    IN_USE("В эксплуатации"),
    IN_STOCK("На складе"),
    IN_REPAIR("На ремонте"),
    WRITTEN_OFF("Списано"),
    RESERVED("Зарезервировано");

    private final String name;

    EquipmentStatusName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
