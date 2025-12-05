package com.onlinetest.questionexam.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultDTO {

    private Long attemptId;
    private Integer attemptNumber;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer percentage;
    private Boolean passed;

    private Integer durationSeconds;
    private String formattedDuration; 

    private Integer maxAttempts;
    private Integer attemptsUsed;
    private Integer attemptsRemaining;

    private Integer passingThreshold; 
    private String status; 

    private LocalDateTime submittedAt;

    private List<QuestionReview> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionReview {
        private Long questionId;
        private Integer position;
        private String selected;
        private Boolean correct;
    }
}
