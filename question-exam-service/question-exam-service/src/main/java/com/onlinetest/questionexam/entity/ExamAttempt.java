package com.onlinetest.questionexam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long companyId;
    @Column(nullable = false) private Long examId;
    @Column(nullable = false) private Long assignmentId;
    @Column(nullable = false) private Long employeeId;

    @Column(nullable = false) private Integer attemptNumber;
    @Column(nullable = false) private Integer totalQuestions;
    @Column(nullable = false) private Integer correctAnswers;
    @Column(nullable = false) private Integer percentage;

    @Column(nullable = false) private Boolean passed;

    @Column(nullable = false) private Integer durationSeconds;

    @Column(nullable = false, length = 30) private String status; // SUBMITTED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;
}
