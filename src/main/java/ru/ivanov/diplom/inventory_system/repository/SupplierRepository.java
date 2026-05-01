package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.Supplier;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByInn(String inn);

    boolean existsByInnAndIdNot(String inn, Long id);
}
