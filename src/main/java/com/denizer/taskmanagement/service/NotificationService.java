package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.denizer.taskmanagement.entity.User;

public interface NotificationService {

    Page<NotificationResponseDto> getMyNotifications(Pageable pageable);

    void markAsRead(Long notificationId);

    void createNotification(User user, String message);
}