package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.DashboardResponseDto;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.denizer.taskmanagement.dto.DashboardResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;

import java.time.LocalDate;
import java.util.List;

import com.denizer.taskmanagement.dto.RecentActivityDto;
import com.denizer.taskmanagement.entity.TaskActivity;
import com.denizer.taskmanagement.entity.TaskActivityAction;

import com.denizer.taskmanagement.dto.RecentNotificationDto;
import com.denizer.taskmanagement.entity.Notification;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TaskFavoriteRepository taskFavoriteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskActivityRepository taskActivityRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null
                )
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnDashboardStatisticsSuccessfully() {

        when(taskRepository.countByUserId(1L))
                .thenReturn(10L);

        when(taskRepository.countByUserIdAndStatus(
                1L,
                com.denizer.taskmanagement.entity.TaskStatus.TODO
        )).thenReturn(3L);

        when(taskRepository.countByUserIdAndStatus(
                1L,
                com.denizer.taskmanagement.entity.TaskStatus.IN_PROGRESS
        )).thenReturn(2L);

        when(taskRepository.countByUserIdAndStatus(
                1L,
                com.denizer.taskmanagement.entity.TaskStatus.COMPLETED
        )).thenReturn(4L);

        when(taskRepository.countByUserIdAndStatus(
                1L,
                com.denizer.taskmanagement.entity.TaskStatus.PENDING
        )).thenReturn(1L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(
                        com.denizer.taskmanagement.entity.TaskStatus.COMPLETED
                )
        )).thenReturn(2L);

        when(taskRepository.countByUserIdAndPriority(
                1L,
                com.denizer.taskmanagement.entity.TaskPriority.HIGH
        )).thenReturn(4L);

        when(taskRepository.countByUserIdAndPriority(
                1L,
                com.denizer.taskmanagement.entity.TaskPriority.MEDIUM
        )).thenReturn(3L);

        when(taskRepository.countByUserIdAndPriority(
                1L,
                com.denizer.taskmanagement.entity.TaskPriority.LOW
        )).thenReturn(3L);

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(2L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(3L);

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        org.mockito.ArgumentMatchers.eq(1L),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.eq(
                                com.denizer.taskmanagement.entity.TaskStatus.COMPLETED
                        )
                ))
                .thenReturn(java.util.List.of());

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(java.util.List.of());

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(java.util.List.of());

        DashboardResponseDto result =
                dashboardService.getDashboard();

        assertEquals(10L, result.getTotalTasks());
        assertEquals(3L, result.getTodoTasks());
        assertEquals(2L, result.getInProgressTasks());
        assertEquals(4L, result.getCompletedTasks());
        assertEquals(1L, result.getPendingTasks());
        assertEquals(2L, result.getOverdueTasks());

        assertEquals(4L, result.getHighPriorityTasks());
        assertEquals(3L, result.getMediumPriorityTasks());
        assertEquals(3L, result.getLowPriorityTasks());

        assertEquals(2L, result.getUnreadNotifications());
        assertEquals(3L, result.getFavoriteTasks());
    }

    @Test
    void shouldReturnUpcomingTasksSuccessfully() {

        Task task = Task.builder()
                .id(10L)
                .title("Upcoming Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2))
                .build();

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L),
                        any(LocalDate.class),
                        eq(TaskStatus.COMPLETED)
                ))
                .thenReturn(List.of(task));

        // Dashboard'ın diğer repository çağrıları
        when(taskRepository.countByUserId(1L)).thenReturn(0L);

        when(taskRepository.countByUserIdAndStatus(anyLong(), any(TaskStatus.class)))
                .thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L),
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                eq(1L),
                any(TaskPriority.class)
        )).thenReturn(0L);

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(0L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        DashboardResponseDto result =
                dashboardService.getDashboard();

        assertEquals(1, result.getUpcomingTasks().size());

        assertEquals(
                "Upcoming Task",
                result.getUpcomingTasks().get(0).getTitle()
        );

        assertEquals(
                TaskStatus.TODO,
                result.getUpcomingTasks().get(0).getStatus()
        );

        assertEquals(
                TaskPriority.HIGH,
                result.getUpcomingTasks().get(0).getPriority()
        );

        assertEquals(
                task.getDueDate(),
                result.getUpcomingTasks().get(0).getDueDate()
        );
    }

    @Test
    void shouldReturnRecentActivitiesSuccessfully() {

        LocalDateTime createdAt = LocalDateTime.now();

        Task task = Task.builder()
                .id(10L)
                .title("Test Task")
                .build();

        TaskActivity activity = new TaskActivity();
        activity.setId(100L);
        activity.setTaskId(10L);
        activity.setUser(user);
        activity.setAction(TaskActivityAction.STATUS_CHANGED);
        activity.setOldValue("TODO");
        activity.setNewValue("COMPLETED");
        activity.setCreatedAt(createdAt);

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activity));

        when(taskRepository.findById(10L))
                .thenReturn(java.util.Optional.of(task));

        // Dashboard'ın diğer repository çağrıları
        when(taskRepository.countByUserId(1L))
                .thenReturn(0L);

        when(taskRepository.countByUserIdAndStatus(
                anyLong(),
                any(TaskStatus.class)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L),
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                eq(1L),
                any(TaskPriority.class)
        )).thenReturn(0L);

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L),
                        any(LocalDate.class),
                        eq(TaskStatus.COMPLETED)
                ))
                .thenReturn(List.of());

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(0L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        DashboardResponseDto result =
                dashboardService.getDashboard();

        assertEquals(1, result.getRecentActivities().size());

        RecentActivityDto recentActivity =
                result.getRecentActivities().get(0);

        assertEquals(100L, recentActivity.getId());
        assertEquals(10L, recentActivity.getTaskId());
        assertEquals("Test Task", recentActivity.getTaskTitle());
        assertEquals(1L, recentActivity.getUserId());
        assertEquals("test@test.com", recentActivity.getUserEmail());
        assertEquals(TaskActivityAction.STATUS_CHANGED, recentActivity.getAction());
        assertEquals("TODO", recentActivity.getOldValue());
        assertEquals("COMPLETED", recentActivity.getNewValue());
        assertEquals(createdAt, recentActivity.getCreatedAt());
    }

    @Test
    void shouldReturnRecentNotificationsSuccessfully() {

        LocalDateTime createdAt = LocalDateTime.now();

        Notification notification = new Notification();
        notification.setId(50L);
        notification.setMessage("Task 'Test Task' is due tomorrow.");
        notification.setRead(false);
        notification.setCreatedAt(createdAt);
        notification.setUser(user);

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(notification));

        // Dashboard'ın diğer repository çağrıları
        when(taskRepository.countByUserId(1L))
                .thenReturn(0L);

        when(taskRepository.countByUserIdAndStatus(
                anyLong(),
                any(TaskStatus.class)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L),
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                eq(1L),
                any(TaskPriority.class)
        )).thenReturn(0L);

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L),
                        any(LocalDate.class),
                        eq(TaskStatus.COMPLETED)
                ))
                .thenReturn(List.of());

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(1L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        DashboardResponseDto result =
                dashboardService.getDashboard();

        assertEquals(1, result.getRecentNotifications().size());

        RecentNotificationDto recentNotification =
                result.getRecentNotifications().get(0);

        assertEquals(50L, recentNotification.getId());
        assertEquals(
                "Task 'Test Task' is due tomorrow.",
                recentNotification.getMessage()
        );
        assertEquals(false, recentNotification.isRead());
        assertEquals(createdAt, recentNotification.getCreatedAt());
    }


    @Test
    void shouldThrowExceptionWhenAuthenticatedUserDoesNotExist() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> dashboardService.getDashboard()
        );

        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(
                taskRepository,
                notificationRepository,
                taskFavoriteRepository,
                taskActivityRepository
        );
    }

    @Test
    void shouldReturnEmptyDashboardListsWhenNoDataExists() {

        when(taskRepository.countByUserId(1L)).thenReturn(0L);

        when(taskRepository.countByUserIdAndStatus(
                anyLong(), any(TaskStatus.class)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                anyLong(), any(TaskPriority.class)
        )).thenReturn(0L);

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
                )).thenReturn(List.of());

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(0L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        DashboardResponseDto result = dashboardService.getDashboard();

        assertEquals(0L, result.getTotalTasks());
        assertEquals(0L, result.getTodoTasks());
        assertEquals(0L, result.getInProgressTasks());
        assertEquals(0L, result.getCompletedTasks());
        assertEquals(0L, result.getPendingTasks());
        assertEquals(0L, result.getOverdueTasks());
        assertEquals(0L, result.getHighPriorityTasks());
        assertEquals(0L, result.getMediumPriorityTasks());
        assertEquals(0L, result.getLowPriorityTasks());
        assertEquals(0L, result.getUnreadNotifications());
        assertEquals(0L, result.getFavoriteTasks());

        assertTrue(result.getUpcomingTasks().isEmpty());
        assertTrue(result.getRecentActivities().isEmpty());
        assertTrue(result.getRecentNotifications().isEmpty());
    }

    @Test
    void shouldReturnRecentActivityWithNullTaskTitleWhenTaskDoesNotExist() {

        TaskActivity activity = new TaskActivity();
        activity.setId(101L);
        activity.setTaskId(999L);
        activity.setUser(user);
        activity.setAction(TaskActivityAction.STATUS_CHANGED);
        activity.setOldValue("TODO");
        activity.setNewValue("COMPLETED");

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(activity));

        when(taskRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        when(taskRepository.countByUserId(1L)).thenReturn(0L);

        when(taskRepository.countByUserIdAndStatus(
                anyLong(), any(TaskStatus.class)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                anyLong(), any(TaskPriority.class)
        )).thenReturn(0L);

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
                )).thenReturn(List.of());

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(0L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        DashboardResponseDto result = dashboardService.getDashboard();

        assertEquals(1, result.getRecentActivities().size());
        assertEquals(999L, result.getRecentActivities().get(0).getTaskId());
        assertEquals(
                null,
                result.getRecentActivities().get(0).getTaskTitle()
        );
    }

    @Test
    void shouldReturnMultipleUpcomingTasksInRepositoryOrder() {

        Task firstTask = Task.builder()
                .id(11L)
                .title("First Upcoming Task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        Task secondTask = Task.builder()
                .id(12L)
                .title("Second Upcoming Task")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(3))
                .build();

        when(taskRepository
                .findByUserIdAndDueDateGreaterThanEqualAndStatusNotOrderByDueDateAsc(
                        eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
                )).thenReturn(List.of(firstTask, secondTask));

        when(taskRepository.countByUserId(1L)).thenReturn(2L);

        when(taskRepository.countByUserIdAndStatus(
                anyLong(), any(TaskStatus.class)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(1L), any(LocalDate.class), eq(TaskStatus.COMPLETED)
        )).thenReturn(0L);

        when(taskRepository.countByUserIdAndPriority(
                anyLong(), any(TaskPriority.class)
        )).thenReturn(0L);

        when(notificationRepository.countByUserIdAndReadFalse(1L))
                .thenReturn(0L);

        when(taskFavoriteRepository.countByUserId(1L))
                .thenReturn(0L);

        when(taskActivityRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        when(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        DashboardResponseDto result = dashboardService.getDashboard();

        assertEquals(2, result.getUpcomingTasks().size());
        assertEquals(
                "First Upcoming Task",
                result.getUpcomingTasks().get(0).getTitle()
        );
        assertEquals(
                "Second Upcoming Task",
                result.getUpcomingTasks().get(1).getTitle()
        );
    }
}