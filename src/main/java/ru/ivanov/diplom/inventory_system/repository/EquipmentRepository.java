package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.Equipment;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByInventoryNumber(String inventoryNumber);

    boolean existsBySerialNumber(String serialNumber);

    Optional<Equipment> findByInventoryNumber(String inventoryNumber);
}
