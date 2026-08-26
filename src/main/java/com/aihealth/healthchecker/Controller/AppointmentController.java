package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.Repo.AppointmentRepo;
import com.aihealth.healthchecker.entity.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentRepo appointmentRepo;

    public AppointmentController(AppointmentRepo appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    @PostMapping
    public Appointment bookAppointment(
            @RequestBody Appointment appointment,
            Authentication authentication
    ) {
        if (authentication != null) {
            appointment.setPatientEmail(authentication.getName());
        }
        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("CONFIRMED");
        }
        return appointmentRepo.save(appointment);
    }

    @GetMapping("/my")
    public List<Appointment> myAppointments(Authentication authentication) {
        List<Appointment> list = appointmentRepo.findByPatientEmail(authentication.getName());
        return autoUpdateCompletedStatuses(list);
    }

    @GetMapping("/all")
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = appointmentRepo.findAll();
        return autoUpdateCompletedStatuses(list);
    }

    @GetMapping("/doctor-appointments")
    public List<Appointment> getDoctorAppointments(
            @RequestParam(required = false) String doctorName,
            Authentication authentication
    ) {
        List<Appointment> list;
        if (doctorName != null && !doctorName.trim().isEmpty()) {
            list = appointmentRepo.findByDoctorNameContainingIgnoreCase(doctorName.trim());
        } else {
            list = appointmentRepo.findAll();
        }
        return autoUpdateCompletedStatuses(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(
            @PathVariable Long id,
            @RequestBody Appointment updatedDetails,
            Authentication authentication
    ) {
        Optional<Appointment> optional = appointmentRepo.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = optional.get();

        // Reschedule date
        if (updatedDetails.getAppointmentDate() != null && !updatedDetails.getAppointmentDate().trim().isEmpty()) {
            appt.setAppointmentDate(updatedDetails.getAppointmentDate().trim());
            // If rescheduled to a future date, reset status to CONFIRMED
            try {
                LocalDate newDate = LocalDate.parse(updatedDetails.getAppointmentDate().trim());
                if (!newDate.isBefore(LocalDate.now())) {
                    appt.setStatus("CONFIRMED");
                }
            } catch (Exception ignored) {}
        }

        // Doctor / Hospital changes if any
        if (updatedDetails.getDoctorName() != null && !updatedDetails.getDoctorName().trim().isEmpty()) {
            appt.setDoctorName(updatedDetails.getDoctorName().trim());
        }
        if (updatedDetails.getHospital() != null && !updatedDetails.getHospital().trim().isEmpty()) {
            appt.setHospital(updatedDetails.getHospital().trim());
        }
        if (updatedDetails.getStatus() != null && !updatedDetails.getStatus().trim().isEmpty()) {
            appt.setStatus(updatedDetails.getStatus().toUpperCase().trim());
        }

        Appointment saved = appointmentRepo.save(appt);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Appointment> optional = appointmentRepo.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Appointment appt = optional.get();
        appt.setStatus(status.toUpperCase().trim());
        appointmentRepo.save(appt);
        return ResponseEntity.ok(appt);
    }

    private List<Appointment> autoUpdateCompletedStatuses(List<Appointment> appointments) {
        LocalDate today = LocalDate.now();
        boolean changed = false;

        for (Appointment a : appointments) {
            if (a.getStatus() == null || "CONFIRMED".equalsIgnoreCase(a.getStatus())) {
                try {
                    LocalDate appDate = LocalDate.parse(a.getAppointmentDate());
                    if (appDate.isBefore(today)) {
                        a.setStatus("COMPLETED");
                        changed = true;
                    }
                } catch (Exception ignored) {}
            }
        }

        if (changed) {
            appointmentRepo.saveAll(appointments);
        }

        return appointments;
    }
}
