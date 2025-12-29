package com.example.utecharity_project.Repository;

import com.example.utecharity_project.Model.Payment_model;
import com.example.utecharity_project.Model.Artical_model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Payment_Repo extends JpaRepository<Payment_model, Long> {

       // Find all payments by display status
       List<Payment_model> findByDisplay(int display);

       // Find by User
       List<Payment_model> findByUser(com.example.utecharity_project.Model.Authorization_model user);

       // Search by ID (number format)
       @Query("SELECT a FROM Payment_model a WHERE a.id = :searchTerm")
       Page<Payment_model> searchById(@Param("searchTerm") Long searchTerm, Pageable pageable);

       // Search by orderId (assuming it's a String, modify if it's a different type)
       // Search by orderId
       @Query("SELECT a FROM Payment_model a WHERE a.orderId LIKE %:searchTerm%")
       Page<Payment_model> searchByOrderId(@Param("searchTerm") String searchTerm, Pageable pageable);

       // --- Aggregation Queries ---

       // Total Revenue by Campaign (Artical)
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE p.artical.id = :campaignId AND p.paymentStatus = 1")
       Double sumRevenueByCampaign(@Param("campaignId") Long campaignId);

       // Total Revenue by Category
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE p.artical.displaycategory = :category AND p.paymentStatus = 1")
       Double sumRevenueByCategory(@Param("category") String category);

       // Total Revenue by Month/Year
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE YEAR(p.paymentTime) = :year AND MONTH(p.paymentTime) = :month AND p.paymentStatus = 1")
       Double sumRevenueByMonthYear(@Param("month") int month, @Param("year") int year);

       // Total Revenue by Quarter
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE YEAR(p.paymentTime) = :year AND MONTH(p.paymentTime) BETWEEN :startMonth AND :endMonth AND p.paymentStatus = 1")
       Double sumRevenueByQuarter(@Param("startMonth") int startMonth, @Param("endMonth") int endMonth,
                     @Param("year") int year);

       // Total Revenue by Year
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE YEAR(p.paymentTime) = :year AND p.paymentStatus = 1")
       Double sumRevenueByYear(@Param("year") int year);

       // Total Revenue by Day
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE YEAR(p.paymentTime) = :year AND MONTH(p.paymentTime) = :month AND DAY(p.paymentTime) = :day AND p.paymentStatus = 1")
       Double sumRevenueByDay(@Param("day") int day, @Param("month") int month, @Param("year") int year);

       // Total Revenue by Hour
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE YEAR(p.paymentTime) = :year AND MONTH(p.paymentTime) = :month AND DAY(p.paymentTime) = :day AND HOUR(p.paymentTime) = :hour AND p.paymentStatus = 1")
       Double sumRevenueByHour(@Param("hour") int hour, @Param("day") int day, @Param("month") int month,
                     @Param("year") int year);

       // Total Revenue (All)
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE p.paymentStatus = 1")
       Double sumTotalRevenue();

       // --- Filter Queries for Listing ---

       @Query("SELECT p FROM Payment_model p WHERE (:campaignId IS NULL OR p.artical.id = :campaignId) " +
                     "AND (:category IS NULL OR p.artical.displaycategory = :category) " +
                     "AND (:startMonth IS NULL OR MONTH(p.paymentTime) >= :startMonth) " +
                     "AND (:endMonth IS NULL OR MONTH(p.paymentTime) <= :endMonth) " +
                     "AND (:year IS NULL OR YEAR(p.paymentTime) = :year)")
       Page<Payment_model> filterPayments(@Param("campaignId") Long campaignId,
                     @Param("category") String category,
                     @Param("startMonth") Integer startMonth,
                     @Param("endMonth") Integer endMonth,
                     @Param("year") Integer year,
                     Pageable pageable);

       // Find successful payments for a specific Artical
       List<Payment_model> findByArtical_IdAndPaymentStatus(Long articalId, int paymentStatus);

       // Statistics for User Profile
       @Query("SELECT SUM(CAST(p.totalPrice AS double)) FROM Payment_model p WHERE p.user.username = :username AND p.paymentStatus = 1")
       Double sumTotalDonatedByUser(@Param("username") String username);

       @Query("SELECT COUNT(DISTINCT p.artical.id) FROM Payment_model p WHERE p.user.username = :username AND p.paymentStatus = 1")
       Long countDistinctProjectsDonatedByUser(@Param("username") String username);

       // Delete all payments by Artical (project)
       void deleteByArtical(Artical_model artical);
}
