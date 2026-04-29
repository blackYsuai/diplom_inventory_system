package ru.ivanov.diplom.inventory_system.entity.enums;

public enum DocumentTypeCode {
    RECEIPT("RECEIPT"),
    MOVEMENT("MOVEMENT"),
    WRITE_OFF("WRITE_OFF"),
    EXPORT("EXPORT");

    private final String code;

    DocumentTypeCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
