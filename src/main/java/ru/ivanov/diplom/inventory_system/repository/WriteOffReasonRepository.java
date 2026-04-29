package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.WriteOffReason;
import org.springframework.stereotype.Repository;

@Repository
public interface WriteOffReasonRepository extends JpaRepository<WriteOffReason, Long> {
}
