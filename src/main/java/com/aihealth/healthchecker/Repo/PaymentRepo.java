package com.aihealth.healthchecker.Repo;

import com.aihealth.healthchecker.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepo extends JpaRepository<Payment,Long> {
}
