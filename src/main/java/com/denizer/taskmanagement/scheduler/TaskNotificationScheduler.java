package com.denizer.taskmanagement.scheduler;

import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskStatus;
import com.denizer.taskmanagement.repository.TaskRepository;
import com.denizer.taskmanagement.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskNotificationScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Europe/Istanbul")
    public void sendDueDateNotifications() {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Task> tasks = taskRepository.findByDueDateAndStatusNot(
                tomorrow.plusDays(1),
                TaskStatus.COMPLETED
        );

        for (Task task : tasks) {

            if (task.getAssignedUser() == null) {
                continue;
            }

            String message = "Task '" + task.getTitle()
                    + "' is due tomorrow.";

            notificationService.createNotification(
                    task.getAssignedUser(),
                    message
            );
        }
    }
}