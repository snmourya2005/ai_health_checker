package com.aihealth.healthchecker.Repo;

import com.aihealth.healthchecker.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialization(String specialization);
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByName(String name);
}
