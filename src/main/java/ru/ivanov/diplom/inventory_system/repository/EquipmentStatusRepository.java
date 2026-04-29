package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.EquipmentStatus;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {

    Optional<EquipmentStatus> findByName(String name);
}
