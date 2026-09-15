package com.denizer.taskmanagement.service.impl;

import com.denizer.taskmanagement.dto.TaskActivityResponseDto;
import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskActivity;
import com.denizer.taskmanagement.entity.TaskActivityAction;
import com.denizer.taskmanagement.entity.User;
import com.denizer.taskmanagement.repository.TaskActivityRepository;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.repository.UserRepository;
import com.denizer.taskmanagement.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public void logActivity(
            Long taskId,
            Long userId,
            String action,
            String oldValue,
            String newValue
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TaskActivity activity = new TaskActivity();

        //activity.setTask(task);
        activity.setTaskId(taskId);
        activity.setUser(user);
        activity.setAction(TaskActivityAction.valueOf(action));
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);

        taskActivityRepository.save(activity);
    }

    @Override
    public List<TaskActivityResponseDto> getTaskActivities(Long taskId) {

        return taskActivityRepository
                .findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(activity -> new TaskActivityResponseDto(
                        activity.getId(),
                        //activity.getTask().getId(),
                        activity.getTaskId(),
                        activity.getUser().getId(),
                        activity.getUser().getEmail(),
                        activity.getAction(),
                        activity.getOldValue(),
                        activity.getNewValue(),
                        activity.getCreatedAt()
                ))
                .toList();
    }
}
