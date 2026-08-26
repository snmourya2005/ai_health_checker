package com.aihealth.healthchecker.Repo;

import com.aihealth.healthchecker.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientEmail(String patientEmail);
    List<Appointment> findByDoctorName(String doctorName);
    List<Appointment> findByDoctorNameContainingIgnoreCase(String doctorName);
}
