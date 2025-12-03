package com.onlinetest.questionexam.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmitRequestDTO {

    @NotNull
    private Long assignmentId;

    @NotNull
    private Long examId;

    @Size(min = 1)
    private List<AnswerDTO> answers;

    // optional client-measured duration
    private Integer elapsedSeconds;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDTO {
        @NotNull private Long questionId;
        @NotNull private String selected; // "A" | "B" | "C" | "D"
        @NotNull private Integer position;
    }
}
