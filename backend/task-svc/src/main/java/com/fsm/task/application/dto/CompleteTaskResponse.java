package com.fsm.task.application.dto;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * DTO for completed task response.
 * Contains task details including completion timestamp and actual duration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for a completed task with duration details")
public class CompleteTaskResponse {
    
    @Schema(description = "Task ID", example = "1")
    private Long id;
    
    @Schema(description = "Task title", example = "Repair HVAC System")
    private String title;
    
    @Schema(description = "Task description", example = "Customer reports heating system not working properly")
    private String description;
    
    @Schema(description = "Client address", example = "123 Main St, Springfield, IL 62701")
    private String clientAddress;
    
    @Schema(description = "Task priority level", example = "HIGH")
    private Priority priority;
    
    @Schema(description = "Estimated duration in minutes", example = "120")
    private Integer estimatedDuration;
    
    @Schema(description = "Task status", example = "COMPLETED")
    private TaskStatus status;
    
    @Schema(description = "Timestamp when the task was assigned")
    private LocalDateTime assignedAt;
    
    @Schema(description = "Timestamp when the task was started")
    private LocalDateTime startedAt;
    
    @Schema(description = "Timestamp when the task was completed")
    private LocalDateTime completedAt;
    
    @Schema(description = "Summary of work performed")
    private String workSummary;
    
    @Schema(description = "Actual duration in minutes (completedAt - startedAt)", example = "135")
    private Long actualDurationMinutes;
    
    /**
     * Converts a ServiceTask entity to CompleteTaskResponse DTO
     * Calculates the actual duration based on startedAt and completedAt timestamps.
     * 
     * @param task the service task entity
     * @param assignedAt the timestamp when the task was assigned
     * @return CompleteTaskResponse DTO
     */
    public static CompleteTaskResponse fromEntity(ServiceTask task, LocalDateTime assignedAt) {
        Long actualDuration = null;
        if (task.getStartedAt() != null && task.getCompletedAt() != null) {
            Duration duration = Duration.between(task.getStartedAt(), task.getCompletedAt());
            actualDuration = duration.toMinutes();
        }
        
        return CompleteTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .clientAddress(task.getClientAddress())
                .priority(task.getPriority())
                .estimatedDuration(task.getEstimatedDuration())
                .status(task.getStatus())
                .assignedAt(assignedAt)
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .workSummary(task.getWorkSummary())
                .actualDurationMinutes(actualDuration)
                .build();
    }
}
