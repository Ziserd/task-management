package com.denizer.taskmanagement.dto;

import lombok.*;

import java.util.List;

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

    private List<UpcomingTaskDto> upcomingTasks;

    private List<RecentActivityDto> recentActivities;

    private List<RecentNotificationDto> recentNotifications;
}