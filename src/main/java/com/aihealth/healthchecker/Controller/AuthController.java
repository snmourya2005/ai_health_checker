package com.aihealth.healthchecker.Controller;

import com.aihealth.healthchecker.DTO.LoginRequest;
import com.aihealth.healthchecker.Security.JwtUtil;
import com.aihealth.healthchecker.Service.UserSev;
import com.aihealth.healthchecker.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserSev userSev;
    private final JwtUtil jwtUtil;

    public AuthController(UserSev userSev, JwtUtil jwtUtil) {
        this.userSev = userSev;
        this.jwtUtil = jwtUtil;
    }
    @PostMapping("/register")
    public ResponseEntity<User>register(@RequestBody User user){
        return ResponseEntity.ok(userSev.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request){
        User user = userSev.login(
                request.getEmail(),
                request.getPassword()
        );
        String token = jwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "role", user.getRole() != null ? user.getRole() : "ROLE_USER",
                        "email", user.getEmail(),
                        "username", user.getUsername() != null ? user.getUsername() : user.getEmail()
                )
        );
    }
}
