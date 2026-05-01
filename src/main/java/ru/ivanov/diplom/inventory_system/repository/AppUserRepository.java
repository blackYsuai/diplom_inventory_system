package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ivanov.diplom.inventory_system.entity.AppUser;

import java.util.List;
import java.util.Optional;


@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(Long employeeId);

    @Query("""
            select distinct u
            from AppUser u
            join fetch u.employee e
            left join fetch e.department
            left join fetch u.permissions
            where u.username = :username
            """)
    Optional<AppUser> findByUsernameWithDetails(@Param("username") String username);

    @Query("""
            select distinct u
            from AppUser u
            join fetch u.employee e
            left join fetch e.department
            left join fetch u.permissions
            where u.id = :id
            """)
    Optional<AppUser> findByIdWithDetails(@Param("id") Long id);

    @Query("""
            select distinct u
            from AppUser u
            join fetch u.employee e
            left join fetch e.department
            left join fetch u.permissions
            order by u.id
            """)
    List<AppUser> findAllWithDetails();
}
