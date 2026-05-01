package ru.ivanov.diplom.inventory_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ivanov.diplom.inventory_system.entity.DocumentItem;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DocumentItemRepository extends JpaRepository<DocumentItem, Long> {

    List<DocumentItem> findAllByDocumentId(Long documentId);

    List<DocumentItem> findAllByEquipmentId(Long equipmentId);

    @Query("""
            select di
            from DocumentItem di
            join fetch di.document d
            join fetch d.documentType dt
            join fetch di.equipment e
            left join fetch di.fromLocation fl
            left join fetch di.toLocation tl
            left join fetch di.fromEmployee fe
            left join fetch di.toEmployee te
            where dt.code in :documentTypeCodes
            """)
    List<DocumentItem> findAllForMovementReport(
            @Param("documentTypeCodes") List<String> documentTypeCodes
    );
    @Query("""
            select distinct di
            from DocumentItem di
            join fetch di.document d
            join fetch d.documentType dt
            join fetch di.equipment e
            left join fetch di.fromLocation fl
            left join fetch di.toLocation tl
            left join fetch di.fromEmployee fe
            left join fetch di.toEmployee te
            where fe.id = :employeeId
               or te.id = :employeeId
            order by d.documentDate desc, d.id desc
            """)
    List<DocumentItem> findAllForEmployeeCabinet(
            @Param("employeeId") Long employeeId
    );

    @Query("""
            select case when count(di) > 0 then true else false end
            from DocumentItem di
            left join di.fromEmployee fe
            left join di.toEmployee te
            where di.document.id = :documentId
              and (fe.id = :employeeId or te.id = :employeeId)
            """)
    boolean existsByDocumentIdAndEmployeeId(
            @Param("documentId") Long documentId,
            @Param("employeeId") Long employeeId
    );

    @Query("""
        select distinct di
        from DocumentItem di
        join fetch di.document d
        join fetch d.documentType dt
        join fetch di.equipment e
        left join fetch di.fromLocation fl
        left join fetch di.toLocation tl
        left join fetch di.fromEmployee fe
        left join fetch di.toEmployee te
        left join fetch di.writeOffReasons wr
        where e.id = :equipmentId
        order by d.documentDate desc, d.id desc
        """)
    List<DocumentItem> findAllByEquipmentIdWithDocumentDetails(
            @Param("equipmentId") Long equipmentId
    );
}
