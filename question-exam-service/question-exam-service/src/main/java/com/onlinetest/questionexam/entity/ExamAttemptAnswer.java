package com.onlinetest.questionexam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_attempt_answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long attemptId;
    @Column(nullable = false) private Long questionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Question.CorrectAnswer selected; // A/B/C/D

    @Column(nullable = false) private Boolean isCorrect;

    @Column(nullable = false) private Integer position; 
}
