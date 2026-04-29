package ru.ivanov.diplom.inventory_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "storage_location",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_storage_location_name_department",
                        columnNames = {"name", "department_id"}
                )
        }
)
public class StorageLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String building;

    @Column(length = 100)
    private String room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
