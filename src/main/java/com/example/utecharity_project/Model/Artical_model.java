package com.example.utecharity_project.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artical_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "goal_amount")
    private double goalAmount;

    @Column(name = "amount_raised")
    private double amountRaised;

    @Column(name = "disbursed_amount")
    private Double disbursedAmount = 0.0;
    private String img;
    private String status;
    private String displaycategory;

    @Column(name = "code")
    private String code;

    // Report fields (for completed projects)
    @Column(name = "report_image")
    private String reportImage;

    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;

    // Link to help request (if created from a request)
    @Column(name = "from_request_id")
    private Long fromRequestId;

    @OneToMany(mappedBy = "artical", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Articaldetail_model> articalDetails;

    @Transient
    private String formattedEndDate;

    public String getFormattedEndDate() {
        return formattedEndDate;
    }

    public void setFormattedEndDate(String formattedEndDate) {
        this.formattedEndDate = formattedEndDate;
    }

    @Transient
    public int getProgressPercentage() {
        if (goalAmount > 0) {
            // Ensure the progress is capped at 100%
            return Math.min(100, (int) Math.round((amountRaised * 100) / goalAmount));
        } else {
            return 0;
        }
    }

    // Check if project is still actively fundraising (not expired AND not reached
    // goal)
    @Transient
    public boolean isActive() {
        boolean notExpired = !LocalDate.now().isAfter(endDate);
        boolean notReachedGoal = amountRaised < goalAmount;
        return notExpired && notReachedGoal;
    }

    // Check if project time has expired
    @Transient
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    @Transient
    public boolean isGoalReached() {
        return amountRaised >= goalAmount;
    }

    // Get display status for frontend
    @Transient
    public String getDisplayStatus() {
        if (isGoalReached()) {
            return "Đã đủ quỹ";
        } else if (isExpired()) {
            return "Đã kết thúc";
        } else {
            return "Đang gây quỹ";
        }
    }

    @Transient
    public boolean hasReport() {
        return reportImage != null || reportContent != null;
    }

    // Auto-generate code before persist based on category
    @PrePersist
    public void prePersist() {
        if (this.code == null || this.code.isEmpty()) {
            String prefix = "DA"; // Default
            if (this.displaycategory != null) {
                switch (this.displaycategory) {
                    case "Y tế":
                        prefix = "YT";
                        break;
                    case "Trẻ em":
                        prefix = "TE";
                        break;
                    case "Giáo dục":
                        prefix = "GD";
                        break;
                    case "Thiên tai":
                        prefix = "TT";
                        break;
                    default:
                        prefix = "DA";
                }
            }
            // Generate code: PREFIX + timestamp (last 6 digits)
            String timestamp = String.valueOf(System.currentTimeMillis());
            this.code = prefix + timestamp.substring(timestamp.length() - 6);
        }
        if (this.status == null || this.status.isEmpty()) {
            this.status = "Đang gây quỹ";
        }
    }
}
