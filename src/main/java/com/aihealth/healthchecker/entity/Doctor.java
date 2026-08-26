package com.aihealth.healthchecker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="doctors")
@Getter
@Setter
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String specialization;

    @Column(nullable = false)
    private String hospital;

    @Column(nullable = false)
    private String location;

    private String email;

    public Doctor() {}

    public Doctor(String name, String specialization, String hospital, String location) {
        this.name = name;
        this.specialization = specialization;
        this.hospital = hospital;
        this.location = location;
    }

    public Doctor(String name, String specialization, String hospital, String location, String email) {
        this.name = name;
        this.specialization = specialization;
        this.hospital = hospital;
        this.location = location;
        this.email = email;
    }
}
