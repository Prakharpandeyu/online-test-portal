package com.onlinetest.questionexam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExamQuestionViewDTO {
    private Long id;          // questionId
    private Long topicId;
    private String topicName;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}
