package com.onlinetest.questionexam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer maxAttempts;
    private Integer attemptsUsed;
    private Integer attemptsRemaining;
    private LocalDateTime submittedAt;

    // Optional per-question review stub; do not include correct answers
    private List<QuestionReview> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionReview {
        private Long questionId;
        private Integer position;
        private String selected; // "A" | "B" | "C" | "D"
        private Boolean correct;
    }
}
