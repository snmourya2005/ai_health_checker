package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.Repo.AppointmentRepo;
import com.aihealth.healthchecker.Repo.DoctorRepo;
import com.aihealth.healthchecker.Repo.UserRepo;
import com.aihealth.healthchecker.entity.Appointment;
import com.aihealth.healthchecker.entity.Doctor;
import com.aihealth.healthchecker.entity.User;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepo doctorRepo;
    private final UserRepo userRepo;
    private final AppointmentRepo appointmentRepo;
    private final PasswordEncoder passwordEncoder;

    public DoctorController(DoctorRepo doctorRepo, UserRepo userRepo, AppointmentRepo appointmentRepo, PasswordEncoder passwordEncoder) {
        this.doctorRepo = doctorRepo;
        this.userRepo = userRepo;
        this.appointmentRepo = appointmentRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initData() {
        // Seed default Admin, Doctor, Customer user accounts if not present
        if (userRepo.findByEmail("admin@test.com").isEmpty()) {
            userRepo.save(new User("Admin", "admin@test.com", passwordEncoder.encode("admin123"), "ROLE_ADMIN"));
        }
        if (userRepo.findByEmail("doctor@test.com").isEmpty()) {
            userRepo.save(new User("Dr. Sarah Jenkins", "doctor@test.com", passwordEncoder.encode("doctor123"), "ROLE_DOCTOR"));
        }
        if (userRepo.findByEmail("user@test.com").isEmpty()) {
            userRepo.save(new User("Patient Alex", "user@test.com", passwordEncoder.encode("user123"), "ROLE_USER"));
        }

        if (doctorRepo.count() == 0) {
            doctorRepo.save(new Doctor("Dr. Sarah Jenkins", "Cardiology", "City Heart Care Hospital", "New York", "doctor@test.com"));
        }
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorRepo.findAll();
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getDoctorByEmail(@RequestParam String email) {
        Optional<Doctor> optionalDoctor = doctorRepo.findByEmail(email);
        if (optionalDoctor.isPresent()) {
            return ResponseEntity.ok(optionalDoctor.get());
        }
        // Fallback: look by doctor user's username
        Optional<User> doctorUser = userRepo.findByEmail(email);
        if (doctorUser.isPresent()) {
            Optional<Doctor> byName = doctorRepo.findByName(doctorUser.get().getUsername());
            if (byName.isPresent()) {
                return ResponseEntity.ok(byName.get());
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addDoctor(@RequestBody Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Doctor name is required."));
        }

        // Verification: Admin can ONLY create a Doctor profile if a registered User with ROLE_DOCTOR already exists!
        Optional<User> doctorUser = Optional.empty();
        if (doctor.getEmail() != null && !doctor.getEmail().trim().isEmpty()) {
            doctorUser = userRepo.findByEmail(doctor.getEmail().trim());
        } else {
            doctorUser = userRepo.findByUsername(doctor.getName().trim());
        }

        if (doctorUser.isEmpty() || !doctorUser.get().getRole().equalsIgnoreCase("ROLE_DOCTOR")) {
            String identifier = (doctor.getEmail() != null && !doctor.getEmail().trim().isEmpty())
                    ? doctor.getEmail()
                    : doctor.getName();
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "Cannot add doctor profile: No registered Doctor account found for '" + identifier +
                    "'. The practitioner must first register an account with role 'Doctor' before an administrator can create their medical profile."
            ));
        }

        doctor.setEmail(doctorUser.get().getEmail());
        Doctor saved = doctorRepo.save(doctor);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor updatedDetails) {
        Optional<Doctor> optionalDoctor = doctorRepo.findById(id);
        if (optionalDoctor.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Doctor not found with id: " + id));
        }

        Doctor existing = optionalDoctor.get();
        String oldName = existing.getName();

        if (updatedDetails.getName() != null && !updatedDetails.getName().trim().isEmpty()) {
            existing.setName(updatedDetails.getName().trim());
        }
        if (updatedDetails.getSpecialization() != null && !updatedDetails.getSpecialization().trim().isEmpty()) {
            existing.setSpecialization(updatedDetails.getSpecialization().trim());
        }
        if (updatedDetails.getHospital() != null && !updatedDetails.getHospital().trim().isEmpty()) {
            existing.setHospital(updatedDetails.getHospital().trim());
        }
        if (updatedDetails.getLocation() != null && !updatedDetails.getLocation().trim().isEmpty()) {
            existing.setLocation(updatedDetails.getLocation().trim());
        }
        if (updatedDetails.getEmail() != null && !updatedDetails.getEmail().trim().isEmpty()) {
            existing.setEmail(updatedDetails.getEmail().trim());
        }

        Doctor saved = doctorRepo.save(existing);

        // If doctor name was modified, update their active appointments to match
        if (oldName != null && !oldName.equalsIgnoreCase(saved.getName())) {
            List<Appointment> appts = appointmentRepo.findByDoctorNameContainingIgnoreCase(oldName);
            for (Appointment a : appts) {
                a.setDoctorName(saved.getName());
            }
            if (!appts.isEmpty()) {
                appointmentRepo.saveAll(appts);
            }
        }

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        Optional<Doctor> optionalDoctor = doctorRepo.findById(id);
        if (optionalDoctor.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Doctor not found with id: " + id));
        }

        Doctor doctor = optionalDoctor.get();
        List<Appointment> appointments = appointmentRepo.findByDoctorNameContainingIgnoreCase(doctor.getName());

        // Check if doctor has any active / confirmed appointments
        LocalDate today = LocalDate.now();
        long activeAppointments = appointments.stream()
                .filter(a -> {
                    if (a.getStatus() == null) return true;
                    boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(a.getStatus());
                    if (!isConfirmed) return false;
                    try {
                        LocalDate appDate = LocalDate.parse(a.getAppointmentDate());
                        return !appDate.isBefore(today);
                    } catch (Exception e) {
                        return true;
                    }
                })
                .count();

        if (activeAppointments > 0) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "error", "Cannot delete doctor: " + doctor.getName() + " has " + activeAppointments +
                    " active/upcoming appointment(s). Please complete or cancel them before removing this doctor."
            ));
        }

        doctorRepo.deleteById(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Doctor " + doctor.getName() + " removed successfully!"));
    }

    @GetMapping("/specialization/{specialization}")
    public List<Doctor> getDoctorsBySpecialization(@PathVariable String specialization) {
        return doctorRepo.findBySpecialization(specialization);
    }
}
