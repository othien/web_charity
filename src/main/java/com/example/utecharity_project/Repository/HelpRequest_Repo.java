package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.HelpRequest_model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpRequest_Repo extends JpaRepository<HelpRequest_model, Long> {
}
