package ru.ivanov.diplom.inventory_system.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportDataType;
import ru.ivanov.diplom.inventory_system.entity.enums.ExportStatus;
import ru.ivanov.diplom.inventory_system.entity.enums.TargetAccountingSystem;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accounting_export")
public class AccountingExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exported_at", nullable = false)
    private LocalDateTime exportedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExportStatus status;

    @Column(name = "result_message", columnDefinition = "TEXT")
    private String resultMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_system", nullable = false, length = 50)
    private TargetAccountingSystem targetSystem;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_type", nullable = false, length = 100)
    private ExportDataType exportType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private InventoryDocument document;
}
