package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Follow_model;
import com.example.utecharity_project.Model.Authorization_model;
import com.example.utecharity_project.Model.Artical_model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Follow_Repo extends JpaRepository<Follow_model, Long> {
    List<Follow_model> findByUser(Authorization_model user);

    boolean existsByUserAndProject(Authorization_model user, Artical_model project);

    void deleteByUserAndProject(Authorization_model user, Artical_model project);

    void deleteByProject(Artical_model project);
}
