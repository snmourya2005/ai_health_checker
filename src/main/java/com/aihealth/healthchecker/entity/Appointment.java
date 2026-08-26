package com.aihealth.healthchecker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientEmail;
    private String doctorName;
    private String hospital;
    private String appointmentDate;
    private String status = "CONFIRMED"; // CONFIRMED, COMPLETED, CANCELLED

    public Appointment() {}

    public Appointment(String patientEmail, String doctorName, String hospital, String appointmentDate, String status) {
        this.patientEmail = patientEmail;
        this.doctorName = doctorName;
        this.hospital = hospital;
        this.appointmentDate = appointmentDate;
        this.status = status != null ? status : "CONFIRMED";
    }
}
