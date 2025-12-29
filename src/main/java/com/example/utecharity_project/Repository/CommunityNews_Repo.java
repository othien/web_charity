package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Communitynews_model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface CommunityNews_Repo extends JpaRepository<Communitynews_model, Long> {
    // Tìm kiếm theo ID
    @Query("SELECT c FROM Communitynews_model c WHERE c.id = :searchTerm")
    Page<Communitynews_model> searchById(@Param("searchTerm") Long searchTerm, Pageable pageable);

    // Tìm kiếm theo tiêu đề
    @Query("SELECT c FROM Communitynews_model c WHERE c.title_news LIKE %:searchTerm%")
    Page<Communitynews_model> searchByTitle(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Comprehensive filter query - newest first (default)
    @Query("SELECT c FROM Communitynews_model c WHERE " +
            "(:searchTerm IS NULL OR c.title_news LIKE %:searchTerm% OR c.sub_titlenews LIKE %:searchTerm%) AND " +
            "(:fromDate IS NULL OR c.date_update >= :fromDate) AND " +
            "(:toDate IS NULL OR c.date_update <= :toDate) " +
            "ORDER BY c.date_update DESC")
    Page<Communitynews_model> filterNews(
            @Param("searchTerm") String searchTerm,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    // Filter with sort by title
    @Query("SELECT c FROM Communitynews_model c WHERE " +
            "(:searchTerm IS NULL OR c.title_news LIKE %:searchTerm% OR c.sub_titlenews LIKE %:searchTerm%) AND " +
            "(:fromDate IS NULL OR c.date_update >= :fromDate) AND " +
            "(:toDate IS NULL OR c.date_update <= :toDate) " +
            "ORDER BY c.title_news ASC")
    Page<Communitynews_model> filterNewsSortByTitle(
            @Param("searchTerm") String searchTerm,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    // Filter with oldest first
    @Query("SELECT c FROM Communitynews_model c WHERE " +
            "(:searchTerm IS NULL OR c.title_news LIKE %:searchTerm% OR c.sub_titlenews LIKE %:searchTerm%) AND " +
            "(:fromDate IS NULL OR c.date_update >= :fromDate) AND " +
            "(:toDate IS NULL OR c.date_update <= :toDate) " +
            "ORDER BY c.date_update ASC")
    Page<Communitynews_model> filterNewsOldestFirst(
            @Param("searchTerm") String searchTerm,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);
}
