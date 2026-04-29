package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.InventoryDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InventoryDocumentRepository extends JpaRepository<InventoryDocument, Long> {

    Optional<InventoryDocument> findByDocumentNumber(String documentNumber);

    List<InventoryDocument> findAllByOrderByDocumentDateDescIdDesc();

}
