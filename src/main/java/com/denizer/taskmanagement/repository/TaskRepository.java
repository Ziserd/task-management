package com.denizer.taskmanagement.repository;

import com.denizer.taskmanagement.entity.Task;
import com.denizer.taskmanagement.entity.TaskPriority;
import com.denizer.taskmanagement.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);
    Page<Task> findByUserId(Long userId, Pageable pageable);
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
    Page<Task> findByPriority(TaskPriority priority, Pageable pageable);
    Page<Task> findByStatusAndPriority(TaskStatus status, TaskPriority priority, Pageable pageable);
    Page<Task> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);
    Page<Task> findByUserIdAndPriority(Long userId, TaskPriority priority, Pageable pageable);
    Page<Task> findByUserIdAndStatusAndPriority(
            Long userId,
            TaskStatus status,
            TaskPriority priority,
            Pageable pageable
    );
    Page<Task> findByAssignedUserId(Long userId, Pageable pageable);

    Page<Task> findByDueDateBeforeAndStatusNot(
            LocalDate date,
            TaskStatus status,
            Pageable pageable
    );

    Page<Task> findByUserIdAndDueDateBeforeAndStatusNot(
            Long userId,
            LocalDate date,
            TaskStatus status,
            Pageable pageable
    );
    @Query("""
    SELECT t FROM Task t
    WHERE (
        :search = ''
        OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (:status IS NULL OR t.status = :status)
    AND (:priority IS NULL OR t.priority = :priority)
    AND t.dueDate <= :dueBefore
    AND t.dueDate >= :dueAfter
    """)
    Page<Task> searchTasks(
            @Param("search") String search,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("dueBefore") LocalDate dueBefore,
            @Param("dueAfter") LocalDate dueAfter,
            Pageable pageable
    );
    @Query("""
        SELECT t FROM Task t
        WHERE t.user.id = :userId
        AND (:search IS NULL
            OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:status IS NULL OR t.status = :status)
        AND (:priority IS NULL OR t.priority = :priority)
        AND t.dueDate <= :dueBefore
        AND t.dueDate >= :dueAfter
        """)
    Page<Task> searchTasksByUserId(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("dueBefore") LocalDate dueBefore,
            @Param("dueAfter") LocalDate dueAfter,
            Pageable pageable
    );


}