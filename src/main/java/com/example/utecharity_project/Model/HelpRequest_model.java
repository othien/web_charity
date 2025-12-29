package com.example.utecharity_project.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelpRequest_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Beneficiary Info
    private String beneficiaryName;
    private String beneficiaryPhone;

    // Reporter Info (Person reporting the case)
    private String reporterName;
    private String reporterPhone;
    private String reporterEmail;
    private String relationship; // Relationship with beneficiary

    @Column(columnDefinition = "TEXT")
    private String description;

    // Status: 0=Pending, 1=Approved, 2=Rejected
    private int status;
}
