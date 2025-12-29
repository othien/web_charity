package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Artical_model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Charitycontent_Repo extends JpaRepository<Artical_model, Long> {
        Page<Artical_model> findAll(Pageable pageable);

        @Query("SELECT a FROM Artical_model a WHERE a.id = :searchTerm")
        Page<Artical_model> searchById(@Param("searchTerm") Long searchTerm, Pageable pageable);

        @Query("SELECT a FROM Artical_model a WHERE a.title LIKE %:searchTerm%")
        Page<Artical_model> searchByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

        List<Artical_model> findByCode(String code);

        // Filter method for Dashboard (replacing FundraisingCampaign's version)
        @Query("SELECT a FROM Artical_model a WHERE " +
                        "(:keyword IS NULL OR :keyword = '' OR a.title LIKE %:keyword% OR CAST(a.id AS string) LIKE %:keyword% OR a.code LIKE %:keyword%) AND "
                        +
                        "(:category IS NULL OR :category = '' OR a.displaycategory = :category) AND " +
                        "(:status IS NULL OR :status = '' OR a.status = :status)")
        Page<Artical_model> filterArticals(@Param("keyword") String keyword,
                        @Param("category") String category,
                        @Param("status") String status,
                        Pageable pageable);

}
