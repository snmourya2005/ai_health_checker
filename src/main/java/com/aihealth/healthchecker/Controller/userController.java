package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.Repo.UserRepo;
import com.aihealth.healthchecker.Service.UserSev;
import com.aihealth.healthchecker.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class userController {
    public final UserSev userSev;
    public final UserRepo userRepo;
    public final PasswordEncoder passwordEncoder;

    public userController(UserSev userSev, UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userSev = userSev;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userSev.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@RequestBody User user, Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Only administrators can create new admin accounts.");
        }
        User created = userSev.createAdminUser(user);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/doctor-accounts")
    public List<User> getRegisteredDoctorAccounts() {
        return userRepo.findByRole("ROLE_DOCTOR");
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updateReq, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<User> optionalUser = userRepo.findByEmail(authentication.getName());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        if (updateReq.getUsername() != null && !updateReq.getUsername().trim().isEmpty()) {
            user.setUsername(updateReq.getUsername().trim());
        }
        if (updateReq.getPassword() != null && !updateReq.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateReq.getPassword().trim()));
        }

        User saved = userRepo.save(user);
        return ResponseEntity.ok(java.util.Map.of(
                "message", "Profile updated successfully!",
                "username", saved.getUsername(),
                "email", saved.getEmail(),
                "role", saved.getRole()
        ));
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userSev.getAllUsers();
    }
}
