package com.denizer.taskmanagement.repository;

import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);
    List<Task> findByStatus(TaskStatus status);
}