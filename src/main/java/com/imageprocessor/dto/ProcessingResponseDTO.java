package com.imageprocessor.dto;

import com.imageprocessor.model.ProcessingTask.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingResponseDTO {
    private Long taskId;
    private Long imageId;
    private String originalFilename;
    private TaskStatus status;
    private String resultImageUrl;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private ProcessingConfigDTO config;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingConfigDTO {
        private boolean resizeEnabled;
        private Integer resizePercentage;
        private boolean grayscaleEnabled;
    }
}