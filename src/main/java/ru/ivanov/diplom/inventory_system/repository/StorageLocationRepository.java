package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.StorageLocation;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageLocationRepository extends JpaRepository<StorageLocation, Long> {

    boolean existsByNameAndDepartmentId(String name, Long departmentId);

    boolean existsByNameAndDepartmentIdAndIdNot(String name, Long departmentId, Long id);
}