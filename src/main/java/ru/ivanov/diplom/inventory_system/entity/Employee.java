package ru.ivanov.diplom.inventory_system.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(length = 150)
    private String position;

    @Column(length = 50)
    private String phone;

    @Column(unique = true, length = 150)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public String getFullName() {
        StringBuilder result = new StringBuilder();

        if (lastName != null) {
            result.append(lastName);
        }

        if (firstName != null) {
            result.append(" ").append(firstName);
        }

        if (middleName != null && !middleName.isBlank()) {
            result.append(" ").append(middleName);
        }

        return result.toString().trim();
    }
}
