package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.ExamAttemptAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAttemptAnswerRepository extends JpaRepository<ExamAttemptAnswer, Long> {
    List<ExamAttemptAnswer> findByAttemptIdOrderByPositionAsc(Long attemptId);
}
