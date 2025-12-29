package com.example.utecharity_project.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Articaldetail_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Legacy fields - kept for backward compatibility
    @Column(columnDefinition = "TEXT")
    private String content_1;

    private String img_content;

    @Column(columnDefinition = "TEXT")
    private String content_2;

    @Column(columnDefinition = "TEXT")
    private String content_3;

    private String img_content2;

    // New dynamic content field - stores JSON array of blocks
    // Format: [{"type": "text", "content": "..."}, {"type": "image", "url": "..."}]
    @Column(columnDefinition = "LONGTEXT")
    private String dynamicContent;

    @ManyToOne
    @JoinColumn(name = "artical_id", referencedColumnName = "id")
    private Artical_model artical;
}
