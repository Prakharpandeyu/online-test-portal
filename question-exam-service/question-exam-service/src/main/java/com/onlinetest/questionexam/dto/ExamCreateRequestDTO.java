package com.onlinetest.questionexam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExamCreateRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @Min(value = 0, message = "Passing percentage must be between 0 and 100")
    @Max(value = 100, message = "Passing percentage must be between 0 and 100")
    private Integer passingPercentage;

    @NotEmpty(message = "At least one topic is required")
    @Valid
    private List<ExamTopicRequestDTO> topics;

    public int getTotalQuestions() {
        return topics == null ? 0 : topics.stream()
                .mapToInt(ExamTopicRequestDTO::getQuestionsCount)
                .sum();
    }

    public List<Long> getTopicIds() {
        return topics == null ? List.of() : topics.stream()
                .map(ExamTopicRequestDTO::getTopicId)
                .toList();
    }

    @Data
    public static class ExamTopicRequestDTO {
        @NotNull(message = "Topic ID is required")
        private Long topicId;

        @NotNull(message = "Questions count is required")
        @Min(value = 1, message = "Questions count must be at least 1")
        private Integer questionsCount;
    }
}
