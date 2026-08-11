package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.UserRequestDto;
import com.denizer.taskmanagement.dto.UserResponseDto;

import java.util.List;

public interface UserService {
    UserResponseDto createUser(UserRequestDto request);

    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers();

    UserResponseDto updateUser(Long id, UserRequestDto request);

    void deleteUser(Long id);
}

