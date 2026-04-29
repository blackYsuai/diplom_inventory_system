package ru.ivanov.diplom.inventory_system.dto.exporting;

public record ExportDownloadResult(
        byte[] content,
        String fileName
) {
}
