package ru.ivanov.diplom.inventory_system.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ivanov.diplom.inventory_system.dto.report.AmortizationReportResponse;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentMovementReportRow;
import ru.ivanov.diplom.inventory_system.dto.report.EquipmentStateReportRow;
import ru.ivanov.diplom.inventory_system.service.ReportPdfService;
import ru.ivanov.diplom.inventory_system.service.ReportService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ReportPdfService reportPdfService;

    @GetMapping("/equipment-state")
    public List<EquipmentStateReportRow> getEquipmentStateReport(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long responsibleEmployeeId
    ) {
        return reportService.getEquipmentStateReport(
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );
    }

    @GetMapping("/equipment-movement")
    public List<EquipmentMovementReportRow> getEquipmentMovementReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,

            @RequestParam(required = false)
            Long equipmentId,

            @RequestParam(required = false)
            String documentTypeCode
    ) {
        return reportService.getEquipmentMovementReport(
                dateFrom,
                dateTo,
                equipmentId,
                documentTypeCode
        );
    }
    @GetMapping("/amortization")
    public AmortizationReportResponse getAmortizationReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate reportDate,

            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long responsibleEmployeeId
    ) {
        return reportService.getAmortizationReport(
                reportDate,
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );
    }
    @GetMapping("/amortization/download")
    public ResponseEntity<byte[]> downloadAmortizationReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate reportDate,

            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long responsibleEmployeeId
    ) {
        byte[] pdf = reportPdfService.generateAmortizationReportPdf(
                reportDate,
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"amortization-report.pdf\""
                )
                .body(pdf);
    }

    @GetMapping("/equipment-state/download")
    public ResponseEntity<byte[]> downloadEquipmentStateReport(
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long responsibleEmployeeId
    ) {
        byte[] pdf = reportPdfService.generateEquipmentStateReportPdf(
                statusId,
                categoryId,
                locationId,
                responsibleEmployeeId
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"equipment-state-report.pdf\""
                )
                .body(pdf);
    }

    @GetMapping("/equipment-movement/download")
    public ResponseEntity<byte[]> downloadEquipmentMovementReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,

            @RequestParam(required = false)
            Long equipmentId,

            @RequestParam(required = false)
            String documentTypeCode
    ) {
        byte[] pdf = reportPdfService.generateEquipmentMovementReportPdf(
                dateFrom,
                dateTo,
                equipmentId,
                documentTypeCode
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"equipment-movement-report.pdf\""
                )
                .body(pdf);
    }
}
