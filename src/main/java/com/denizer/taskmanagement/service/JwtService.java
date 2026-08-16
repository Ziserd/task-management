package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.entity.User;

public interface JwtService {

    String generateToken(User user);
}