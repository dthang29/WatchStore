package com.pet_project.watchstore.repository;

import com.pet_project.watchstore.entity.PasswordResetRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PasswordResetRequest p where p.email = :email")
    Optional<PasswordResetRequest> findByEmailForUpdate(@Param("email") String email);
}
