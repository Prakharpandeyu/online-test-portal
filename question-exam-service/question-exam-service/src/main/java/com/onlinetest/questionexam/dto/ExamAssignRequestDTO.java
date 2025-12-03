package com.onlinetest.questionexam.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamAssignRequestDTO {
    @NotNull private Long examId;
    @NotEmpty private List<Long> employeeIds;
    private LocalDateTime startTime;   // optional, null = available immediately
    private LocalDateTime endTime;     // optional, null = no end constraint
    @NotNull @Min(1) private Integer maxAttempts = 1;
}
