package com.denizer.taskmanagement.controller;

import com.denizer.taskmanagement.dto.TaskActivityResponseDto;
import com.denizer.taskmanagement.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @GetMapping("/{taskId}/activities")
    public ResponseEntity<List<TaskActivityResponseDto>> getTaskActivities(
            @PathVariable Long taskId) {

        return ResponseEntity.ok(
                taskActivityService.getTaskActivities(taskId)
        );
    }
}