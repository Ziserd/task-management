package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.entity.User;

public interface JwtService {

    String generateToken(User user);
    String extractEmail(String token);
    String extractRole(String token);
}