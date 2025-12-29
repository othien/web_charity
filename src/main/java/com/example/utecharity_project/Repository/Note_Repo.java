package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Note_model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Note_Repo extends JpaRepository<Note_model, Long> {
    List<Note_model> findByDate(String date);
}

