package com.denizer.taskmanagement.repository;

import com.denizer.taskmanagement.entity.TaskFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskFavoriteRepository extends JpaRepository<TaskFavorite, Long> {

    Optional<TaskFavorite> findByUserIdAndTaskId(Long userId, Long taskId);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    Page<TaskFavorite> findByUserId(Long userId, Pageable pageable);
}