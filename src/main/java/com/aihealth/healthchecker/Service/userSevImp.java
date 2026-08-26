package com.aihealth.healthchecker.Service;

import com.aihealth.healthchecker.Repo.UserRepo;
import com.aihealth.healthchecker.entity.User;
import com.aihealth.healthchecker.exception.InvalidCredentialsException;
import com.aihealth.healthchecker.exception.userAlreadyExistException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class userSevImp implements UserSev {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public userSevImp(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(User user) {
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new userAlreadyExistException("Email already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Public registration: prevent unauthorized creation of ROLE_ADMIN
        String reqRole = user.getRole() != null ? user.getRole().toUpperCase().trim() : "";
        if (reqRole.contains("ADMIN")) {
            // Default to ROLE_USER for public registration attempts
            user.setRole("ROLE_USER");
        } else if (reqRole.contains("DOCTOR")) {
            user.setRole("ROLE_DOCTOR");
        } else {
            user.setRole("ROLE_USER");
        }

        return userRepo.save(user);
    }

    @Override
    public User createAdminUser(User adminUser) {
        if (userRepo.findByEmail(adminUser.getEmail()).isPresent()) {
            throw new userAlreadyExistException("Email already registered");
        }
        adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
        adminUser.setRole("ROLE_ADMIN");
        return userRepo.save(adminUser);
    }

    @Override
    public User login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}
