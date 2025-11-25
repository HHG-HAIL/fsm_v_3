package com.fsm.task.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for completing a task with work summary.
 * Used by technicians to mark their assigned tasks as completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for completing a task with work summary")
public class CompleteTaskRequest {
    
    @NotBlank(message = "Work summary is required")
    @Size(min = 10, message = "Work summary must be at least 10 characters")
    @Schema(
            description = "Summary of work performed (minimum 10 characters)", 
            example = "Replaced faulty HVAC compressor and tested system. All functions operating normally.",
            required = true
    )
    private String workSummary;
}
