package ru.ivanov.diplom.inventory_system.dto.exporting;


import ru.ivanov.diplom.inventory_system.entity.enums.ExportStatus;

public record ExportSendResponse(
        ExportStatus status,
        int httpStatusCode,
        String message,
        String endpointUrl,
        String responseBody
) {
}
