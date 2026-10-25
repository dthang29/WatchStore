package com.pet_project.watchstore.repository;

import com.pet_project.watchstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.time.LocalDateTime;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where p.active = true and (
                :keyword is null or lower(p.name) like lower(concat('%', :keyword, '%'))
                or lower(p.brand) like lower(concat('%', :keyword, '%'))
            )
            """)
    Page<Product> findPublicProducts(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where p.active = true and p.lastRestockedAt >= :since
            """)
    Page<Product> findNewArrivals(@Param("since") LocalDateTime since, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where :keyword is null or lower(p.name) like lower(concat('%', :keyword, '%'))
                or lower(p.brand) like lower(concat('%', :keyword, '%'))
            """)
    Page<Product> findAdminProducts(@Param("keyword") String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Optional<Product> findByIdAndActiveTrue(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id and p.active = true")
    Optional<Product> findActiveByIdForUpdate(@Param("id") Integer id);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable pageable);
}
