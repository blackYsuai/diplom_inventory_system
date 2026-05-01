package ru.ivanov.diplom.inventory_system.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.ivanov.diplom.inventory_system.entity.Employee;
import org.springframework.stereotype.Repository;
import java.util.Optional;



@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);
}