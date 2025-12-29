package com.example.utecharity_project.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String activity;
    private String detail_activity;
    private LocalDateTime datetime;

    @Transient
    private String formattedDatetime;

    // Getters and setters cho formattedDatetime
    public String getFormattedDatetime() {
        return formattedDatetime;
    }

    public void setFormattedDatetime(String formattedDatetime) {
        this.formattedDatetime = formattedDatetime;
    }
}


