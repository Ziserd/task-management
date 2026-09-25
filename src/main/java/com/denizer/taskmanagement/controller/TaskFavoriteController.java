package com.denizer.taskmanagement.controller;

import com.denizer.taskmanagement.dto.TaskResponseDto;
import com.denizer.taskmanagement.service.TaskFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskFavoriteController {

    private final TaskFavoriteService taskFavoriteService;

    @PostMapping("/{taskId}/favorite")
    public void addFavorite(@PathVariable Long taskId) {
        taskFavoriteService.addFavorite(taskId);
    }

    @DeleteMapping("/{taskId}/favorite")
    public void removeFavorite(@PathVariable Long taskId) {
        taskFavoriteService.removeFavorite(taskId);
    }

    @GetMapping("/favorites")
    public Page<TaskResponseDto> getMyFavorites(Pageable pageable) {
        return taskFavoriteService.getMyFavorites(pageable);
    }
}