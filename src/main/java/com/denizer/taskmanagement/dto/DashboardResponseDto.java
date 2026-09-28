package com.denizer.taskmanagement.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponseDto {

    private long totalTasks;

    private long todoTasks;

    private long inProgressTasks;

    private long completedTasks;

    private long pendingTasks;

    private long overdueTasks;

    private long highPriorityTasks;

    private long mediumPriorityTasks;

    private long lowPriorityTasks;

    private long unreadNotifications;

    private long favoriteTasks;
}