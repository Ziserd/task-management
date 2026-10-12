
package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskActivityResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskActivity;
import com.denizer.taskmanagement.entity.TaskActivityAction;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.TaskActivityRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskActivityServiceImplTest {

    @Mock
    private TaskActivityRepository taskActivityRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskActivityServiceImpl taskActivityService;

    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .build();

        task = Task.builder()
                .id(10L)
                .title("Test Task")
                .user(user)
                .build();
    }

    @Test
    void shouldLogActivitySuccessfully() {
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        taskActivityService.logActivity(
                10L,
                1L,
                "STATUS_CHANGED",
                "TODO",
                "COMPLETED"
        );

        ArgumentCaptor<TaskActivity> captor =
                ArgumentCaptor.forClass(TaskActivity.class);

        verify(taskActivityRepository).save(captor.capture());

        TaskActivity savedActivity = captor.getValue();

        assertEquals(10L, savedActivity.getTaskId());
        assertEquals(user, savedActivity.getUser());
        assertEquals(
                TaskActivityAction.STATUS_CHANGED,
                savedActivity.getAction()
        );
        assertEquals("TODO", savedActivity.getOldValue());
        assertEquals("COMPLETED", savedActivity.getNewValue());
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskActivityService.logActivity(
                        99L, 1L, "STATUS_CHANGED", "TODO", "COMPLETED"
                )
        );

        assertEquals("Task not found", exception.getMessage());

        verify(userRepository, never()).findById(anyLong());
        verifyNoInteractions(taskActivityRepository);
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskActivityService.logActivity(
                        10L, 99L, "STATUS_CHANGED", "TODO", "COMPLETED"
                )
        );

        assertEquals("User not found", exception.getMessage());

        verifyNoInteractions(taskActivityRepository);
    }

    @Test
    void shouldThrowExceptionWhenActionIsInvalid() {
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> taskActivityService.logActivity(
                        10L, 1L, "INVALID_ACTION", null, null
                )
        );

        verifyNoInteractions(taskActivityRepository);
    }

    @Test
    void shouldReturnTaskActivitiesSuccessfully() {
        LocalDateTime createdAt = LocalDateTime.now();

        TaskActivity activity = new TaskActivity();
        activity.setId(100L);
        activity.setTaskId(10L);
        activity.setUser(user);
        activity.setAction(TaskActivityAction.STATUS_CHANGED);
        activity.setOldValue("TODO");
        activity.setNewValue("COMPLETED");
        activity.setCreatedAt(createdAt);

        when(taskActivityRepository
                .findByTaskIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(activity));

        List<TaskActivityResponseDto> result =
                taskActivityService.getTaskActivities(10L);

        assertEquals(1, result.size());

        TaskActivityResponseDto dto = result.get(0);

        assertEquals(100L, dto.getId());
        assertEquals(10L, dto.getTaskId());
        assertEquals(1L, dto.getUserId());
        assertEquals("test@test.com", dto.getUserEmail());
        assertEquals(TaskActivityAction.STATUS_CHANGED, dto.getAction());
        assertEquals("TODO", dto.getOldValue());
        assertEquals("COMPLETED", dto.getNewValue());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void shouldReturnEmptyListWhenTaskHasNoActivities() {
        when(taskActivityRepository
                .findByTaskIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<TaskActivityResponseDto> result =
                taskActivityService.getTaskActivities(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskActivityRepository)
                .findByTaskIdOrderByCreatedAtDesc(10L);
    }
}