package com.aihealth.healthchecker.Service;

import com.aihealth.healthchecker.entity.User;

import java.util.List;

public interface UserSev {
    User registerUser(User user);

    User createAdminUser(User adminUser);

    User login(String email, String password);

    List<User> getAllUsers();
}
