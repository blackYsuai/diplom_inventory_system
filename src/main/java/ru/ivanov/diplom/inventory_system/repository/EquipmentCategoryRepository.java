package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.EquipmentCategory;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

}
