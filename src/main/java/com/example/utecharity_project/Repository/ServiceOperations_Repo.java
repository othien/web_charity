package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Service_model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServiceOperations_Repo extends JpaRepository<Service_model, Long> {

    // Search by title (case insensitive)
    @Query("SELECT s FROM Service_model s WHERE LOWER(s.title_service) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Service_model> findByTitle_serviceContainingIgnoreCase(String title, Pageable pageable);

    // Search by ID (returns the matching page)
    Page<Service_model> findById(Long id, Pageable pageable);
}

