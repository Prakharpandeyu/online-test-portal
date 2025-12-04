package com.onlinetest.questionexam.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Company ID is required")
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @NotNull(message = "Topic ID is required")
    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @NotBlank(message = "Question text is required")
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotBlank(message = "Option A is required")
    @Column(name = "option_a", nullable = false, length = 500)
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Column(name = "option_b", nullable = false, length = 500)
    private String optionB;

    @NotBlank(message = "Option C is required")
    @Column(name = "option_c", nullable = false, length = 500)
    private String optionC;

    @NotBlank(message = "Option D is required")
    @Column(name = "option_d", nullable = false, length = 500)
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_answer", nullable = false)
    private CorrectAnswer correctAnswer;

    @NotNull(message = "Created by user ID is required")
    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @NotBlank(message = "Created by role is required")
    @Column(name = "created_by_role", nullable = false, length = 50)
    private String createdByRole;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", insertable = false, updatable = false)
    private Topic topic;

    public enum CorrectAnswer { A, B, C, D }
}
