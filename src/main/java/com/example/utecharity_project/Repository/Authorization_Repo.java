package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Authorization_model;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface Authorization_Repo extends JpaRepository<Authorization_model, Long> {
        Optional<Authorization_model> findByUsername(String username);

        Optional<Authorization_model> findByEmail(String email);

        boolean existsByUsername(String username);

        boolean existsByEmail(String email);

        @Query("SELECT u FROM Authorization_model u WHERE " +
                        "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.email LIKE %:keyword%) AND " +
                        "(:role IS NULL OR u.roles LIKE %:role%)")
        Page<Authorization_model> filterUsers(
                        @Param("keyword") String keyword,
                        @Param("role") String role,
                        Pageable pageable);
}
