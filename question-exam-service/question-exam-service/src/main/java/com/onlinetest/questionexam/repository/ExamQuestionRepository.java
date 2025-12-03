package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {
    List<ExamQuestion> findByExamIdOrderByPositionAsc(Long examId);
    void deleteByExamId(Long examId);

}
