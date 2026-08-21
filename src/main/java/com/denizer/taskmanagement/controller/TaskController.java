package com.denizer.taskmanagement.controller;

import com.denizer.taskmanagement.dto.TaskRequestDto;
import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping("/user/{userId}")
    public Page<TaskResponseDto> getTasksByUserId(
            @PathVariable Long userId,
            Pageable pageable) {

        return taskService.getTasksByUserId(userId, pageable);
    }

    @GetMapping
    public Page<TaskResponseDto> getTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable) {

        if (search != null && !search.isBlank()) {
            return taskService.searchTasks(
                    search,
                    status,
                    priority,
                    pageable
            );
        }

        if (status != null && priority != null) {
            return taskService.getTasksByStatusAndPriority(
                    status,
                    priority,
                    pageable
            );
        }

        if (status != null) {
            return taskService.getTasksByStatus(
                    status,
                    pageable
            );
        }

        if (priority != null) {
            return taskService.getTasksByPriority(
                    priority,
                    pageable
            );
        }

        return taskService.getTasks(pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto request) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }


}