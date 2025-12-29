package com.example.utecharity_project.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment_model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    private String transactionId;
    private String totalPrice;
    private LocalDateTime paymentTime;
    private int paymentStatus;
    private int display;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "artical_id")
    private Artical_model artical;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "user_id")
    private Authorization_model user;
}
