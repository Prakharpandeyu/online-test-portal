package com.onlinetest.questionexam.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_assignments")
@Data
public class ExamAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long companyId;
    @Column(nullable = false) private Long examId;
    @Column(nullable = false) private Long employeeId;

    @Column(nullable = false) private Long assignedBy;
    @Column(nullable = false, length = 50) private String assignedByRole;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(nullable = false) private Integer maxAttempts = 1;

    @Column(nullable = false) private Integer attemptsUsed = 0;

    @Column(length = 15)     private String lastResult;       // "PASSED" | "FAILED"
    @Column                  private Integer lastPercentage;  // 0-100
    @Column                  private LocalDateTime lastSubmittedAt;

    @Column(nullable = false, length = 30) private String status = "ASSIGNED";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedDate = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
