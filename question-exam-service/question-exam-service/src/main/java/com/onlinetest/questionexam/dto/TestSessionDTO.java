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
public class TestSessionDTO {
    private Long assignmentId;
    private Long examId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDateTime startedAt;
    private LocalDateTime endsAt;
    private List<ExamQuestionViewDTO> questions;
}
