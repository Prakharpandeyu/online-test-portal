package com.onlinetest.questionexam.controller;

import com.onlinetest.questionexam.dto.ApiResponseDTO;
import com.onlinetest.questionexam.dto.ExamAssignRequestDTO;
import com.onlinetest.questionexam.dto.ExamAssignmentResponseDTO;
import com.onlinetest.questionexam.dto.TestSessionDTO;
import com.onlinetest.questionexam.service.ExamAssignmentService;
import com.onlinetest.questionexam.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exam-assignments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExamAssignmentController {

    private final ExamAssignmentService service;
    private final JWTUtil jwtUtil;

    // Admin/Super Admin: assign exam to employees
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<List<ExamAssignmentResponseDTO>>> assignExam(
            @Valid @RequestBody ExamAssignRequestDTO req,
            @RequestHeader("Authorization") String token) {

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long companyId   = jwtUtil.extractCompanyId(jwt);
        Long adminUserId = jwtUtil.extractUserId(jwt);
        String role      = jwtUtil.extractRole(jwt);

        List<ExamAssignmentResponseDTO> data =
                service.assignExam(req, companyId, adminUserId, role, jwt);

        return ResponseEntity.ok(ApiResponseDTO.success("Exam assigned successfully", data));
    }

    // Employee: list my assignments
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDTO<List<ExamAssignmentResponseDTO>>> myAssignments(
            @RequestHeader("Authorization") String token) {

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long companyId  = jwtUtil.extractCompanyId(jwt);
        Long employeeId = jwtUtil.extractUserId(jwt);

        List<ExamAssignmentResponseDTO> data =
                service.listMyAssignments(companyId, employeeId);

        return ResponseEntity.ok(ApiResponseDTO.success("Assignments retrieved", data));
    }

    // Employee: start exam
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{assignmentId}/start")
    public ResponseEntity<ApiResponseDTO<TestSessionDTO>> startAssignedExam(
            @PathVariable Long assignmentId,
            @RequestHeader("Authorization") String token) {

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        Long companyId  = jwtUtil.extractCompanyId(jwt);
        Long employeeId = jwtUtil.extractUserId(jwt);

        TestSessionDTO session =
                service.startAssignedExam(assignmentId, companyId, employeeId);

        return ResponseEntity.ok(ApiResponseDTO.success("Exam started successfully", session));
    }
}
