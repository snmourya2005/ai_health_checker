package com.aihealth.healthchecker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stripeSessionId;
    private String paymentStatus;
    private Long amount;
    private String currency;
    private String userEmail;

    private LocalDateTime createdAt = LocalDateTime.now();
}
