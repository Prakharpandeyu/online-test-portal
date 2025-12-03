package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, Long> {
}
