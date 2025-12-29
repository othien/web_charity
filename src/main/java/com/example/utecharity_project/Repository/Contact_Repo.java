package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Contact_model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Contact_Repo extends JpaRepository<Contact_model, Long> {

    // Search
    @Query("SELECT c FROM Contact_model c WHERE c.user_fullname LIKE %:searchTerm% OR c.user_email LIKE %:searchTerm% OR c.user_comment LIKE %:searchTerm%")
    Page<Contact_model> search(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Contact_model c WHERE :type IS NULL OR c.type = :type")
    Page<Contact_model> findByType(@Param("type") String type, Pageable pageable);

    @Query("SELECT c FROM Contact_model c WHERE c.user_email = :email AND c.type = :type")
    java.util.List<Contact_model> findByEmailAndType(@Param("email") String email, @Param("type") String type);

    // Comprehensive filter query
    @Query("SELECT c FROM Contact_model c WHERE " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:searchTerm IS NULL OR c.user_fullname LIKE %:searchTerm% OR c.user_email LIKE %:searchTerm% " +
            "    OR c.user_phone LIKE %:searchTerm% OR c.user_comment LIKE %:searchTerm% " +
            "    OR c.beneficiaryName LIKE %:searchTerm% OR c.reporterName LIKE %:searchTerm%) AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:relationship IS NULL OR c.relationship = :relationship) " +
            "ORDER BY c.id DESC")
    Page<Contact_model> filterContacts(
            @Param("type") String type,
            @Param("searchTerm") String searchTerm,
            @Param("status") Integer status,
            @Param("relationship") String relationship,
            Pageable pageable);
}