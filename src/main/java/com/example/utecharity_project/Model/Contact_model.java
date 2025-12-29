package com.example.utecharity_project.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String user_fullname;
    private String user_email;
    private String user_phone;

    @Column(columnDefinition = "TEXT")
    private String user_comment;

    private String type; // "Yêu cầu hỗ trợ" or "Liên hệ chung"

    // Additional fields for Support Request
    private String beneficiaryName;
    private String beneficiaryPhone;
    private String reporterName;
    private String reporterPhone;
    private String relationship;

    @Column(columnDefinition = "TEXT")
    private String verificationImage; // Optional verification image URL

    private Integer status; // 0: Pending, 1: Processed
}
