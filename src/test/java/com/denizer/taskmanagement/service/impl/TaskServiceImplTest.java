package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.dto.TaskStatisticsResponseDto;
import com.denizer.taskmanagement.entity.Role;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ForbiddenException;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.NotificationService;
import com.denizer.taskmanagement.service.TaskActivityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskActivityService taskActivityService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User user;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
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
    void shouldCreateTaskSuccessfully() {

        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Test Task");
        request.setDescription("Test description");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        Task savedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        when(taskRepository.save(any(Task.class)))
                .thenReturn(savedTask);

        TaskResponseDto result =
                taskService.createTask(request);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test description", result.getDescription());
        assertEquals(TaskStatus.TODO, result.getStatus());
        assertEquals(TaskPriority.HIGH, result.getPriority());
        assertEquals(request.getDueDate(), result.getDueDate());
        assertEquals(1L, result.getUserId());

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .save(any(Task.class));

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "TASK_CREATED",
                        null,
                        null
                );
    }

    @Test
    void shouldGetOwnTaskByIdSuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("My Task")
                .description("My task description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(3))
                .user(user)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        TaskResponseDto result =
                taskService.getTaskById(1L);

        assertNotNull(result);

        assertEquals(1L, result.getId());
        assertEquals("My Task", result.getTitle());
        assertEquals("My task description", result.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
        assertEquals(TaskPriority.MEDIUM, result.getPriority());
        assertEquals(1L, result.getUserId());

        verify(taskRepository)
                .findById(1L);

        verify(userRepository)
                .findByEmail(user.getEmail());
    }

    @Test
    void shouldNotGetAnotherUsersTask() {

        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(Role.USER)
                .build();

        Task task = Task.builder()
                .id(2L)
                .title("Another User Task")
                .description("This task belongs to another user")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(5))
                .user(anotherUser)
                .build();

        when(taskRepository.findById(2L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> taskService.getTaskById(2L)
        );

        assertEquals(
                "You are not allowed to access this task.",
                exception.getMessage()
        );

        verify(taskRepository)
                .findById(2L);

        verify(userRepository)
                .findByEmail(user.getEmail());
    }

    @Test
    void shouldUpdateTaskStatusSuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .build();

        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Test Task");
        request.setDescription("Test description");
        request.setStatus(TaskStatus.COMPLETED);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(task.getDueDate());

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        TaskResponseDto result =
                taskService.updateTask(1L, request);

        assertNotNull(result);

        assertEquals(TaskStatus.COMPLETED, result.getStatus());

        verify(taskRepository)
                .findById(1L);

        verify(taskRepository)
                .save(task);

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "STATUS_CHANGED",
                        "TODO",
                        "COMPLETED"
                );

        verify(notificationService, never())
                .createNotification(
                        any(User.class),
                        anyString()
                );
    }

    @Test
    void shouldSendNotificationWhenTaskStatusChanges() {

        User assignedUser = User.builder()
                .id(2L)
                .email("assigned@example.com")
                .firstName("Assigned")
                .lastName("User")
                .role(Role.USER)
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .assignedUser(assignedUser)
                .build();

        TaskRequestDto request = new TaskRequestDto();
        request.setTitle("Test Task");
        request.setDescription("Test description");
        request.setStatus(TaskStatus.COMPLETED);
        request.setPriority(TaskPriority.HIGH);
        request.setDueDate(task.getDueDate());

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenReturn(task);

        TaskResponseDto result =
                taskService.updateTask(1L, request);

        assertNotNull(result);
        assertEquals(TaskStatus.COMPLETED, result.getStatus());

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "STATUS_CHANGED",
                        "TODO",
                        "COMPLETED"
                );

        verify(notificationService)
                .createNotification(
                        assignedUser,
                        "Task 'Test Task' status changed to COMPLETED."
                );
    }

    @Test
    void shouldDeleteTaskSuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("Task to Delete")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        taskService.deleteTask(1L);

        verify(taskRepository)
                .findById(1L);

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "TASK_DELETED",
                        "Task to Delete",
                        null
                );

        verify(taskRepository)
                .delete(task);
    }

    @Test
    void shouldNotDeleteAnotherUsersTask() {

        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .firstName("Another")
                .lastName("User")
                .role(Role.USER)
                .build();

        Task task = Task.builder()
                .id(2L)
                .title("Another User Task")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now().plusDays(5))
                .user(anotherUser)
                .build();

        when(taskRepository.findById(2L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        assertThrows(
                ForbiddenException.class,
                () -> taskService.deleteTask(2L)
        );

        verify(taskRepository, never())
                .delete(task);

        verify(taskActivityService, never())
                .logActivity(
                        anyLong(),
                        anyLong(),
                        anyString(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldAssignTaskSuccessfully() {

        User assignedUser = User.builder()
                .id(2L)
                .email("assigned@example.com")
                .firstName("Assigned")
                .lastName("User")
                .role(Role.USER)
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Task to Assign")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findById(2L))
                .thenReturn(java.util.Optional.of(assignedUser));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response =
                taskService.assignTask(1L, 2L);

        assertEquals(1L, response.getId());
        assertEquals(2L, response.getAssignedUserId());
        assertEquals(
                "assigned@example.com",
                response.getAssignedUserEmail()
        );

        verify(taskRepository)
                .findById(1L);

        verify(userRepository)
                .findById(2L);

        verify(taskRepository)
                .save(task);

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "TASK_ASSIGNED",
                        null,
                        "2"
                );

        verify(notificationService)
                .createNotification(
                        assignedUser,
                        "Task 'Task to Assign' has been assigned to you."
                );
    }

    @Test
    void shouldReassignTaskSuccessfully() {

        User oldAssignedUser = User.builder()
                .id(2L)
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .role(Role.USER)
                .build();

        User newAssignedUser = User.builder()
                .id(3L)
                .email("new@example.com")
                .firstName("New")
                .lastName("User")
                .role(Role.USER)
                .build();

        Task task = Task.builder()
                .id(1L)
                .title("Task to Reassign")
                .description("Task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .assignedUser(oldAssignedUser)
                .build();

        when(taskRepository.findById(1L))
                .thenReturn(java.util.Optional.of(task));

        when(userRepository.findById(3L))
                .thenReturn(java.util.Optional.of(newAssignedUser));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response =
                taskService.assignTask(1L, 3L);

        assertEquals(1L, response.getId());
        assertEquals(3L, response.getAssignedUserId());
        assertEquals(
                "new@example.com",
                response.getAssignedUserEmail()
        );

        verify(taskActivityService)
                .logActivity(
                        1L,
                        1L,
                        "TASK_ASSIGNED",
                        "2",
                        "3"
                );

        verify(notificationService)
                .createNotification(
                        newAssignedUser,
                        "Task 'Task to Reassign' has been assigned to you."
                );
    }

    @Test
    void shouldGetTaskStatisticsForUser() {

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.countByUserId(user.getId()))
                .thenReturn(10L);

        when(taskRepository.countByUserIdAndStatus(
                user.getId(), TaskStatus.TODO))
                .thenReturn(3L);

        when(taskRepository.countByUserIdAndStatus(
                user.getId(), TaskStatus.PENDING))
                .thenReturn(1L);

        when(taskRepository.countByUserIdAndStatus(
                user.getId(), TaskStatus.IN_PROGRESS))
                .thenReturn(4L);

        when(taskRepository.countByUserIdAndStatus(
                user.getId(), TaskStatus.COMPLETED))
                .thenReturn(2L);

        when(taskRepository.countByUserIdAndDueDateBeforeAndStatusNot(
                eq(user.getId()),
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED)))
                .thenReturn(2L);

        TaskStatisticsResponseDto response =
                taskService.getTaskStatistics();

        assertEquals(10L, response.getTotalTasks());
        assertEquals(3L, response.getTodoTasks());
        assertEquals(1L, response.getPendingTasks());
        assertEquals(4L, response.getInProgressTasks());
        assertEquals(2L, response.getCompletedTasks());
        assertEquals(2L, response.getOverdueTasks());
    }

    @Test
    void shouldGetTaskStatisticsForAdmin() {

        User admin = User.builder()
                .id(99L)
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(java.util.Optional.of(admin));

        when(taskRepository.count())
                .thenReturn(20L);

        when(taskRepository.countByStatus(TaskStatus.TODO))
                .thenReturn(5L);

        when(taskRepository.countByStatus(TaskStatus.PENDING))
                .thenReturn(2L);

        when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS))
                .thenReturn(7L);

        when(taskRepository.countByStatus(TaskStatus.COMPLETED))
                .thenReturn(6L);

        when(taskRepository.countByDueDateBeforeAndStatusNot(
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED)))
                .thenReturn(4L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        admin.getEmail(), null
                )
        );

        TaskStatisticsResponseDto response =
                taskService.getTaskStatistics();

        assertEquals(20L, response.getTotalTasks());
        assertEquals(5L, response.getTodoTasks());
        assertEquals(2L, response.getPendingTasks());
        assertEquals(7L, response.getInProgressTasks());
        assertEquals(6L, response.getCompletedTasks());
        assertEquals(4L, response.getOverdueTasks());
    }

    @Test
    void shouldGetAssignedTasksSuccessfully() {

        Task task1 = Task.builder()
                .id(1L)
                .title("Assigned Task 1")
                .description("First assigned task")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .user(user)
                .assignedUser(user)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .title("Assigned Task 2")
                .description("Second assigned task")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(5))
                .user(user)
                .assignedUser(user)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        PageImpl<Task> taskPage =
                new PageImpl<>(
                        java.util.List.of(task1, task2),
                        pageable,
                        2
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.findByAssignedUserId(
                user.getId(),
                pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response =
                taskService.getAssignedTasks(pageable);

        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getContent().size());

        assertEquals(
                "Assigned Task 1",
                response.getContent().get(0).getTitle()
        );

        assertEquals(
                "Assigned Task 2",
                response.getContent().get(1).getTitle()
        );

        assertEquals(
                user.getId(),
                response.getContent().get(0).getAssignedUserId()
        );

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .findByAssignedUserId(
                        user.getId(),
                        pageable
                );
    }

    @Test
    void shouldGetTasksByStatusSuccessfully() {

        Task completedTask = Task.builder()
                .id(1L)
                .title("Completed Task")
                .description("Completed task description")
                .status(TaskStatus.COMPLETED)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2))
                .user(user)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        PageImpl<Task> taskPage =
                new PageImpl<>(
                        java.util.List.of(completedTask),
                        pageable,
                        1
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.findByUserIdAndStatus(
                user.getId(),
                TaskStatus.COMPLETED,
                pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response =
                taskService.getTasksByStatus(
                        TaskStatus.COMPLETED,
                        pageable
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                "Completed Task",
                response.getContent().get(0).getTitle()
        );

        assertEquals(
                TaskStatus.COMPLETED,
                response.getContent().get(0).getStatus()
        );

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .findByUserIdAndStatus(
                        user.getId(),
                        TaskStatus.COMPLETED,
                        pageable
                );
    }

    @Test
    void shouldGetTasksByPrioritySuccessfully() {

        Task highPriorityTask = Task.builder()
                .id(1L)
                .title("High Priority Task")
                .description("High priority task description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(2))
                .user(user)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        PageImpl<Task> taskPage =
                new PageImpl<>(
                        java.util.List.of(highPriorityTask),
                        pageable,
                        1
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.findByUserIdAndPriority(
                user.getId(),
                TaskPriority.HIGH,
                pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response =
                taskService.getTasksByPriority(
                        TaskPriority.HIGH,
                        pageable
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                "High Priority Task",
                response.getContent().get(0).getTitle()
        );

        assertEquals(
                TaskPriority.HIGH,
                response.getContent().get(0).getPriority()
        );

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .findByUserIdAndPriority(
                        user.getId(),
                        TaskPriority.HIGH,
                        pageable
                );
    }

    @Test

    void shouldGetTasksByStatusAndPrioritySuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("High Priority In Progress Task")
                .description("Task description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .user(user)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        PageImpl<Task> taskPage =
                new PageImpl<>(
                        java.util.List.of(task),
                        pageable,
                        1
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.findByUserIdAndStatusAndPriority(
                user.getId(),
                TaskStatus.IN_PROGRESS,
                TaskPriority.HIGH,
                pageable
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response =
                taskService.getTasksByStatusAndPriority(
                        TaskStatus.IN_PROGRESS,
                        TaskPriority.HIGH,
                        pageable
                );

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                "High Priority In Progress Task",
                response.getContent().get(0).getTitle()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                response.getContent().get(0).getStatus()
        );

        assertEquals(
                TaskPriority.HIGH,
                response.getContent().get(0).getPriority()
        );

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .findByUserIdAndStatusAndPriority
                        (user.getId(), TaskStatus.IN_PROGRESS, TaskPriority.HIGH, pageable);
    }

    @Test
    void shouldGetOverdueTasksSuccessfully() {

        Task overdueTask = Task.builder()
                .id(1L)
                .title("Overdue Task")
                .description("Overdue task description")
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(2))
                .user(user)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);

        PageImpl<Task> taskPage =
                new PageImpl<>(
                        java.util.List.of(overdueTask),
                        pageable,
                        1
                );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(java.util.Optional.of(user));

        when(taskRepository.findByUserIdAndDueDateBeforeAndStatusNot(
                eq(user.getId()),
                any(LocalDate.class),
                eq(TaskStatus.COMPLETED),
                eq(pageable)
        )).thenReturn(taskPage);

        Page<TaskResponseDto> response =
                taskService.getOverdueTasks(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                "Overdue Task",
                response.getContent().get(0).getTitle()
        );

        assertEquals(
                TaskStatus.IN_PROGRESS,
                response.getContent().get(0).getStatus()
        );

        assertTrue(
                response.getContent().get(0)
                        .getDueDate()
                        .isBefore(LocalDate.now())
        );

        verify(userRepository)
                .findByEmail(user.getEmail());

        verify(taskRepository)
                .findByUserIdAndDueDateBeforeAndStatusNot(
                        eq(user.getId()),
                        any(LocalDate.class),
                        eq(TaskStatus.COMPLETED),
                        eq(pageable)
                );
    }

}