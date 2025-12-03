package com.onlinetest.questionexam.controller;

import com.onlinetest.questionexam.dto.ApiResponseDTO;
import com.onlinetest.questionexam.dto.ExamResultDTO;
import com.onlinetest.questionexam.dto.ExamSubmitRequestDTO;
import com.onlinetest.questionexam.service.ExamSubmissionService;
import com.onlinetest.questionexam.util.SecurityContextUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exams")
public class ExamSubmissionController {

    private final ExamSubmissionService submissionService;

    @PostMapping("/{examId}/submit")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponseDTO<ExamResultDTO>> submit(
            @PathVariable Long examId,
            @Valid @RequestBody ExamSubmitRequestDTO req
    ) {
        Long companyId = SecurityContextUtil.getCompanyId();
        Long employeeId = SecurityContextUtil.getUserId();
        // Ensure path and body alignment
        if (!examId.equals(req.getExamId())) {
            return ResponseEntity.badRequest().body(ApiResponseDTO.error("Mismatched examId"));
        }
        ExamResultDTO result = submissionService.submitFinalAnswers(companyId, employeeId, req);
        return ResponseEntity.ok(ApiResponseDTO.success("Exam submitted", result));
    }
}
