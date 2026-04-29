package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ivanov.diplom.inventory_system.entity.Department;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
