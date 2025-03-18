package com.imageprocessor.repository;

import com.imageprocessor.model.Image;
import com.imageprocessor.model.ProcessingTask;
import com.imageprocessor.model.ProcessingTask.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, Long> {
    Page<ProcessingTask> findByOriginalImage_Owner_Id(Long userId, Pageable pageable);
    List<ProcessingTask> findByStatus(TaskStatus status);

    @Query("SELECT pt FROM ProcessingTask pt WHERE pt.status = :status AND pt.createdAt < :timestamp")
    List<ProcessingTask> findStuckTasks(@Param("status") TaskStatus status, @Param("timestamp") LocalDateTime timestamp);

    List<ProcessingTask> findByOriginalImage(Image image);

    @Query("SELECT COUNT(pt) FROM ProcessingTask pt WHERE pt.originalImage.owner.id = :userId AND pt.createdAt >= :startOfDay")
    long countTasksCreatedToday(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
}