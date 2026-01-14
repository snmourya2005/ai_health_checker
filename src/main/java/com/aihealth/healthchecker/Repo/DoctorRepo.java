package com.aihealth.healthchecker.Repo;

import com.aihealth.healthchecker.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepo extends JpaRepository<Doctor,Long> {
}
