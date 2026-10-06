package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskFavorite;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.exception.ConflictException;
import com.denizer.taskmanagement.repository.TaskFavoriteRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.denizer.taskmanagement.dto.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class TaskFavoriteServiceImplTest {

    @Mock
    private TaskFavoriteRepository taskFavoriteRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskFavoriteServiceImpl taskFavoriteService;

    private User user;
    private Task task;

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

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null
                )
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddFavoriteSuccessfully() {

        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(taskFavoriteRepository.existsByUserIdAndTaskId(1L, 10L))
                .thenReturn(false);

        taskFavoriteService.addFavorite(10L);

        verify(taskFavoriteRepository).save(any(TaskFavorite.class));
    }

    @Test
    void shouldThrowConflictWhenTaskIsAlreadyFavorite() {

        when(taskRepository.findById(10L))
                .thenReturn(Optional.of(task));

        when(taskFavoriteRepository.existsByUserIdAndTaskId(1L, 10L))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> taskFavoriteService.addFavorite(10L)
        );

        verify(taskFavoriteRepository, never())
                .save(any(TaskFavorite.class));
    }

    @Test
    void shouldRemoveFavoriteSuccessfully() {

        TaskFavorite favorite = new TaskFavorite();
        favorite.setUser(user);
        favorite.setTask(task);

        when(taskFavoriteRepository.findByUserIdAndTaskId(1L, 10L))
                .thenReturn(Optional.of(favorite));

        taskFavoriteService.removeFavorite(10L);

        verify(taskFavoriteRepository).delete(favorite);
    }

    @Test
    void shouldGetMyFavoritesSuccessfully() {

        TaskFavorite favorite = new TaskFavorite();
        favorite.setUser(user);
        favorite.setTask(task);

        Page<TaskFavorite> favoritePage =
                new PageImpl<>(List.of(favorite));

        Pageable pageable = PageRequest.of(0, 10);

        when(taskFavoriteRepository.findByUserId(1L, pageable))
                .thenReturn(favoritePage);

        Page<TaskResponseDto> result =
                taskFavoriteService.getMyFavorites(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).getTitle());
        assertEquals(10L, result.getContent().get(0).getId());

        verify(taskFavoriteRepository).findByUserId(1L, pageable);
    }
}