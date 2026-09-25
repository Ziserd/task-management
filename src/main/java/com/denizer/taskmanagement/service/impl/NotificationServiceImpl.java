package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.NotificationResponseDto;
import com.denizer.taskmanagement.entity.Notification;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.NotificationRepository;
import com.denizer.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.denizer.taskmanagement.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public Page<NotificationResponseDto> getMyNotifications(Pageable pageable) {

        User user = getAuthenticatedUser();

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToDto);
    }

    @Override
    public void markAsRead(Long notificationId) {

        User user = getAuthenticatedUser();

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to access this notification");
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    private NotificationResponseDto mapToDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    private User getAuthenticatedUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Burada mevcut UserRepository üzerinden user'ı bulacağız.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void createNotification(User user, String message) {

        Notification notification = new Notification();

        notification.setUser(user);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }
}