package com.example.utecharity_project.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Communitynews_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String img_news;

    private LocalDate date_update;

    @Column(columnDefinition = "TEXT")
    private String title_news;

    @Column(columnDefinition = "TEXT")
    private String sub_titlenews;

    @Column(columnDefinition = "TEXT")
    private String url_artical;
}
