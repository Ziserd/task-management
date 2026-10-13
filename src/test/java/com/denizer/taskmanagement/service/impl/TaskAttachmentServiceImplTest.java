
package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskAttachmentResponseDto;
import com.denizer.taskmanagement.entity.Role;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskAttachment;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ForbiddenException;
import com.denizer.taskmanagement.exception.ResourceNotFoundException;
import com.denizer.taskmanagement.repository.TaskAttachmentRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskAttachmentServiceImplTest {

    @Mock
    private TaskAttachmentRepository taskAttachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskAttachmentServiceImpl taskAttachmentService;

    private User user;
    private User otherUser;
    private Task task;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        otherUser = mock(User.class);
        task = mock(Task.class);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldThrowExceptionWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes()
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskAttachmentService.uploadAttachment(99L, file)
        );

        verifyNoInteractions(taskAttachmentRepository);
    }

    @Test
    void shouldThrowExceptionWhenUploadingEmptyFile() {
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getRole()).thenReturn(Role.USER);
        when(user.getId()).thenReturn(1L);
        when(task.getUser()).thenReturn(user);

        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskAttachmentService.uploadAttachment(10L, file)
        );

        assertEquals("File cannot be empty.", exception.getMessage());
        verify(taskAttachmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenAnotherUserUploadsAttachment() {
        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getRole()).thenReturn(Role.USER);
        when(user.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        when(task.getUser()).thenReturn(otherUser);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "hello".getBytes()
        );

        assertThrows(
                ForbiddenException.class,
                () -> taskAttachmentService.uploadAttachment(10L, file)
        );

        verify(taskAttachmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenListingAttachmentsForMissingTask() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskAttachmentService.getTaskAttachments(99L)
        );

        verifyNoInteractions(taskAttachmentRepository);
    }

    @Test
    void shouldReturnEmptyListWhenTaskHasNoAttachments() {
        when(taskRepository.existsById(10L)).thenReturn(true);
        when(taskAttachmentRepository.findByTaskIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of());

        List<TaskAttachmentResponseDto> result =
                taskAttachmentService.getTaskAttachments(10L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAttachmentDoesNotExist() {
        when(taskAttachmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskAttachmentService.deleteAttachment(99L)
        );

        verify(userRepository, never()).findByEmail(anyString());
        verify(taskAttachmentRepository, never()).delete(any());
    }

    @Test
    void shouldThrowForbiddenExceptionWhenAnotherUserDeletesAttachment() {
        TaskAttachment attachment = new TaskAttachment();
        attachment.setUser(otherUser);

        when(taskAttachmentRepository.findById(20L))
                .thenReturn(Optional.of(attachment));

        when(userRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(user));

        when(user.getRole()).thenReturn(Role.USER);
        when(user.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);

        assertThrows(
                ForbiddenException.class,
                () -> taskAttachmentService.deleteAttachment(20L)
        );

        verify(taskAttachmentRepository, never()).delete(any());
    }
}