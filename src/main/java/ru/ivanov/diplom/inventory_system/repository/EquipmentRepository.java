package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ivanov.diplom.inventory_system.entity.Equipment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsByInventoryNumber(String inventoryNumber);

    boolean existsBySerialNumber(String serialNumber);

    Optional<Equipment> findByInventoryNumber(String inventoryNumber);

    boolean existsByInventoryNumberAndIdNot(String inventoryNumber, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    @Query("""
            select e
            from Equipment e
            join fetch e.category
            join fetch e.status
            join fetch e.location l
            left join fetch l.department
            join fetch e.responsibleEmployee re
            left join fetch re.department
            where re.id = :employeeId
            order by e.id
            """)
    List<Equipment> findAllByResponsibleEmployeeIdWithDetails(
            @Param("employeeId") Long employeeId
    );
}
