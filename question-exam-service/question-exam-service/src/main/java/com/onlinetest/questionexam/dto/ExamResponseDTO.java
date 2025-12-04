package com.onlinetest.questionexam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDTO {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private Integer totalQuestions;
    private Integer durationMinutes;
    private Integer selectedTopicCount;
    private Integer passingPercentage;    
    private LocalDateTime createdDate;

    // Optional, if needed by admin views
    private LocalDateTime updatedDate;
    private Long updatedBy;
    private String updatedByRole;
}
