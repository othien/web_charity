package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Activity_model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface Activity_Repo extends JpaRepository<Activity_model, Long> {
    List<Activity_model> findByUsername(String username);

    List<Activity_model> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    List<Activity_model> findByUsernameAndDatetimeBetween(String username, LocalDateTime start, LocalDateTime end);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Activity_model a WHERE " +
            "(:username IS NULL OR :username = '' OR a.username LIKE %:username%) AND " +
            "(:start IS NULL OR a.datetime >= :start) AND " +
            "(:end IS NULL OR a.datetime <= :end)")
    org.springframework.data.domain.Page<Activity_model> filterActivities(
            @org.springframework.data.repository.query.Param("username") String username,
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end,
            org.springframework.data.domain.Pageable pageable);
}
