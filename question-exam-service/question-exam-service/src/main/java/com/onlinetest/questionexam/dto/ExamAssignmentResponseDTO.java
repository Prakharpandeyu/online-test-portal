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
public class ExamAssignmentResponseDTO {

    private Long id; // assignmentId

    // Exam details
    private String examTitle;
    private String examDescription;
    private Integer totalQuestions;
    private Integer durationMinutes;
    private Integer passingPercentage; // 0-100

    // Assignment status
    private String status; // ASSIGNED / IN_PROGRESS / COMPLETED / EXPIRED / 
    private Integer maxAttempts;

    // Attempts tracking
    private Integer attemptsUsed;       
    private Integer attemptsRemaining;   
    private String lastResult;           
    private Integer lastPercentage;      
    // Time window controls
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // UI helpers
    private Boolean canStart;
    private String statusMessage;
}
