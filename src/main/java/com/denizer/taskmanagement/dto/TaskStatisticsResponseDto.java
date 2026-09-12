package com.denizer.taskmanagement.dto;

public class TaskStatisticsResponseDto {

    private long totalTasks;
    private long todoTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long overdueTasks;

    public TaskStatisticsResponseDto(
            long totalTasks,
            long todoTasks,
            long pendingTasks,
            long inProgressTasks,
            long completedTasks,
            long overdueTasks) {

        this.totalTasks = totalTasks;
        this.todoTasks = todoTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.completedTasks = completedTasks;
        this.overdueTasks = overdueTasks;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public long getTodoTasks() {
        return todoTasks;
    }

    public long getPendingTasks() {
        return pendingTasks;
    }

    public long getInProgressTasks() {
        return inProgressTasks;
    }

    public long getCompletedTasks() {
        return completedTasks;
    }

    public long getOverdueTasks() {
        return overdueTasks;
    }
}