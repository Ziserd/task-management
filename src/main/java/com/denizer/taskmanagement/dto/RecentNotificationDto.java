package com.denizer.taskmanagement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentNotificationDto {

    private Long id;

    private String message;

    private boolean read;

    private LocalDateTime createdAt;
}