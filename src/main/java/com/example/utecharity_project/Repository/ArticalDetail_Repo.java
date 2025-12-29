package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Articaldetail_model;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticalDetail_Repo extends JpaRepository<Articaldetail_model, Long> {
    List<Articaldetail_model> findByartical_id(Long artical_id);
    Articaldetail_model findFirstByArtical_Id(Long artical_id);
}

