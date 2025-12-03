package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    long countByAssignmentId(Long assignmentId);
    Optional<ExamAttempt> findTopByAssignmentIdOrderByAttemptNumberDesc(Long assignmentId);
    List<ExamAttempt> findByAssignmentIdOrderByAttemptNumberAsc(Long assignmentId);
}
