package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.LoginRequestDto;
import com.denizer.taskmanagement.dto.LoginResponseDto;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.denizer.taskmanagement.service.JwtService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user);

        return LoginResponseDto.builder()
                .token(token)
                .build();
    }
}