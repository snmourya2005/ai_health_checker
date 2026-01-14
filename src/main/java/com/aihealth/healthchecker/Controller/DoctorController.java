package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.Repo.DoctorRepo;
import com.aihealth.healthchecker.entity.Doctor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepo doctorRepo;
    public DoctorController(DoctorRepo doctorRepo){
        this.doctorRepo=doctorRepo;
    }
    @PostMapping
    public Doctor addDoctor(@RequestBody Doctor doctor){
        return doctorRepo.save(doctor);
    }
    @GetMapping
    public List<Doctor> getAllDoctors(){
        return doctorRepo.findAll();
    }
}
