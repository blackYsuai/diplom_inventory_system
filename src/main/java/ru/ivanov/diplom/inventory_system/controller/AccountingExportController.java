package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.exporting.*;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportStatus;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;
import ru.ivanov.diplom.inventory_system.service.AccountingExportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN') or hasAuthority('EXPORT_DATA')")
@RequestMapping("/api/export/accounting")
@RequiredArgsConstructor
public class AccountingExportController {

    private final AccountingExportService accountingExportService;

    @PostMapping("/preview")
    public AccountingExportPackage previewExport(
            @Valid @RequestBody ExportAccountingRequest request) {
        return accountingExportService.previewExport(request);
    }

    @PostMapping("/download")
    public ResponseEntity<byte[]> downloadExport(
            @Valid @RequestBody ExportAccountingRequest request) {
        ExportDownloadResult result = accountingExportService.downloadExport(request);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.fileName() + "\""
                )
                .body(result.content());
    }

    @GetMapping("/history")
    public List<ExportHistoryResponse> getExportHistory(
            @RequestParam(required = false) ExportStatus status,
            @RequestParam(required = false) ExportDataType exportType,
            @RequestParam(required = false) TargetAccountingSystem targetSystem,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo
    ) {
        return accountingExportService.getExportHistory(
                status,
                exportType,
                targetSystem,
                dateFrom,
                dateTo
        );
    }

    @PostMapping("/send")
    public ExportSendResponse sendExport(
            @Valid @RequestBody ExportAccountingRequest request) {
        return accountingExportService.sendExport(request);
    }
}