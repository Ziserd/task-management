package com.denizer.taskmanagement.service;

import com.denizer.taskmanagement.dto.TaskResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskFavoriteService {

    void addFavorite(Long taskId);

    void removeFavorite(Long taskId);

    Page<TaskResponseDto> getMyFavorites(Pageable pageable);
}