package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.NotificationResponseDto;
import com.denizer.taskmanagement.entity.Notification;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.NotificationRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnMyNotificationsSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setMessage("Task 'Test Task' is due tomorrow.");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Page<Notification> notificationPage =
                new PageImpl<>(List.of(notification));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable))
                .thenReturn(notificationPage);

        Page<NotificationResponseDto> result =
                notificationService.getMyNotifications(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        NotificationResponseDto dto = result.getContent().get(0);

        assertEquals(1L, dto.getId());
        assertEquals(
                "Task 'Test Task' is due tomorrow.",
                dto.getMessage()
        );
        assertFalse(dto.isRead());
        assertEquals(
                notification.getCreatedAt(),
                dto.getCreatedAt()
        );

        verify(userRepository).findByEmail(user.getEmail());

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                );
    }

    @Test
    void shouldMarkNotificationAsReadSuccessfully() {

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setMessage("Test notification");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(notificationRepository.findById(1L))
                .thenReturn(Optional.of(notification));

        notificationService.markAsRead(1L);

        assertTrue(notification.isRead());

        verify(userRepository).findByEmail(user.getEmail());
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldNotMarkAnotherUsersNotificationAsRead() {

        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .build();

        Notification notification = new Notification();
        notification.setId(2L);
        notification.setUser(anotherUser);
        notification.setMessage("Another user's notification");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(notificationRepository.findById(2L))
                .thenReturn(Optional.of(notification));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.markAsRead(2L)
        );

        assertEquals(
                "You are not allowed to access this notification",
                exception.getMessage()
        );

        assertFalse(notification.isRead());

        verify(userRepository).findByEmail(user.getEmail());
        verify(notificationRepository).findById(2L);
        verify(notificationRepository, never())
                .save(any(Notification.class));
    }

    @Test
    void shouldCreateNotificationSuccessfully() {

        String message = "Task 'Test Task' is due tomorrow.";

        notificationService.createNotification(user, message);

        verify(notificationRepository).save(
                argThat(notification ->
                        notification.getUser().equals(user)
                                && notification.getMessage().equals(message)
                )
        );
    }

}
