package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.LoginRequestDto;
import com.denizer.taskmanagement.dto.LoginResponseDto;

public interface AuthService {

    LoginResponseDto login(LoginRequestDto request);
}