package com.denizer.taskmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}