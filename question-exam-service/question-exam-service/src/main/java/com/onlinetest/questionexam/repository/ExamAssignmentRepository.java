package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.ExamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExamAssignmentRepository extends JpaRepository<ExamAssignment, Long> {
    List<ExamAssignment> findByCompanyIdAndEmployeeIdOrderByCreatedDateDesc(Long companyId, Long employeeId);
    List<ExamAssignment> findByCompanyIdAndEmployeeIdAndStatusIn(Long companyId, Long employeeId, List<String> statuses);
    boolean existsByCompanyIdAndExamIdAndEmployeeId(Long companyId, Long examId, Long employeeId);
    long countByCompanyIdAndExamId(Long companyId, Long examId);
}
