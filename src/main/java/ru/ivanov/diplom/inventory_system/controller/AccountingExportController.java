package ru.ivanov.diplom.inventory_system.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ivanov.diplom.inventory_system.dto.exporting.*;
import ru.ivanov.diplom.inventory_system.service.AccountingExportService;

import java.util.List;

@RestController
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
    public List<ExportHistoryResponse> getExportHistory() {
        return accountingExportService.getExportHistory();
    }

    @PostMapping("/send")
    public ExportSendResponse sendExport(
            @Valid @RequestBody ExportAccountingRequest request) {
        return accountingExportService.sendExport(request);
    }
}