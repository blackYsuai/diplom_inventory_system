package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ivanov.diplom.inventory_system.entity.AccountingExport;

import java.util.List;


@Repository
public interface AccountingExportRepository extends JpaRepository<AccountingExport, Long> {

    List<AccountingExport> findAllByDocumentId(Long documentId);
    List<AccountingExport> findAllByOrderByExportedAtDesc();

}
