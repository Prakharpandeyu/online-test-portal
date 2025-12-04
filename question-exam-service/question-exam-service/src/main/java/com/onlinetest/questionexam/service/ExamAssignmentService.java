package com.onlinetest.questionexam.service;

import com.onlinetest.questionexam.dto.ExamAssignRequestDTO;
import com.onlinetest.questionexam.dto.ExamAssignmentResponseDTO;
import com.onlinetest.questionexam.dto.ExamQuestionViewDTO;
import com.onlinetest.questionexam.dto.TestSessionDTO;
import com.onlinetest.questionexam.entity.Exam;
import com.onlinetest.questionexam.entity.ExamAssignment;
import com.onlinetest.questionexam.entity.ExamQuestion;
import com.onlinetest.questionexam.entity.Question;
import com.onlinetest.questionexam.integration.UserClient;
import com.onlinetest.questionexam.integration.UserClient.UserSummaryDTO;
import com.onlinetest.questionexam.repository.ExamAssignmentRepository;
import com.onlinetest.questionexam.repository.ExamQuestionRepository;
import com.onlinetest.questionexam.repository.ExamRepository;
import com.onlinetest.questionexam.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ExamAssignmentService {

    private final ExamRepository examRepository;
    private final ExamAssignmentRepository examAssignmentRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserClient userClient;

    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_REVOKED = "REVOKED";

    public List<ExamAssignmentResponseDTO> assignExam(
            ExamAssignRequestDTO req,
            Long companyId,
            Long adminUserId,
            String role,
            String jwt
    ) {
        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));

        if (!exam.getCompanyId().equals(companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam does not belong to your company");
        }

        LocalDateTime start = req.getStartTime();
        LocalDateTime end = req.getEndTime();
        if (start != null && end != null && end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date window");
        }

        validateEmployees(req.getEmployeeIds(), companyId, jwt);

        LocalDateTime now = LocalDateTime.now();
        List<ExamAssignmentResponseDTO> responses = new ArrayList<>();

        for (Long empId : req.getEmployeeIds()) {
            if (examAssignmentRepository.existsByCompanyIdAndExamIdAndEmployeeId(companyId, exam.getId(), empId)) {
                continue;
            }

            ExamAssignment a = new ExamAssignment();
            a.setCompanyId(companyId);
            a.setExamId(exam.getId());
            a.setEmployeeId(empId);
            a.setAssignedBy(adminUserId);
            a.setAssignedByRole(role);
            a.setStartTime(start);
            a.setEndTime(end);
            a.setMaxAttempts(req.getMaxAttempts() == null ? 1 : req.getMaxAttempts());
            a.setStatus(STATUS_ASSIGNED);
            a.setCreatedDate(now);
            a.setUpdatedDate(now);

            ExamAssignment saved = examAssignmentRepository.save(a);

            responses.add(mapToDTO(saved, exam, now));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<ExamAssignmentResponseDTO> listMyAssignments(Long companyId, Long employeeId) {
        List<ExamAssignment> assignments = examAssignmentRepository
                .findByCompanyIdAndEmployeeIdOrderByCreatedDateDesc(companyId, employeeId);

        Set<Long> examIds = assignments.stream().map(ExamAssignment::getExamId).collect(Collectors.toSet());
        Map<Long, Exam> examMap = examRepository.findAllById(examIds).stream()
                .collect(Collectors.toMap(Exam::getId, e -> e));

        LocalDateTime now = LocalDateTime.now();

        return assignments.stream()
                .map(a -> mapToDTO(a, examMap.get(a.getExamId()), now))
                .toList();
    }

    private ExamAssignmentResponseDTO mapToDTO(ExamAssignment a, Exam ex, LocalDateTime now) {
        boolean expired = a.getEndTime() != null && now.isAfter(a.getEndTime());

        String finalStatus;
        if (STATUS_COMPLETED.equals(a.getStatus())) {
            finalStatus = STATUS_COMPLETED;
        } else if (expired) {
            finalStatus = "EXPIRED";
        } else {
            finalStatus = a.getStatus();
        }

        return ExamAssignmentResponseDTO.builder()
                .id(a.getId())
                .examTitle(ex.getTitle())
                .examDescription(ex.getDescription())
                .totalQuestions(ex.getTotalQuestions())
                .durationMinutes(ex.getDurationMinutes())
                .passingPercentage(ex.getPassingPercentage())
                .status(finalStatus)
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .canStart(canStartExam(a, now))
                .statusMessage(getStatusMessage(a, now))
                .attemptsUsed(a.getAttemptsUsed()) // keep your logic intact
                .maxAttempts(a.getMaxAttempts())
                .build();
    }

    @Transactional
    public TestSessionDTO startAssignedExam(Long assignmentId, Long companyId, Long employeeId) {
        ExamAssignment a = examAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));

        if (!a.getCompanyId().equals(companyId) || !a.getEmployeeId().equals(employeeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }

        LocalDateTime now = LocalDateTime.now();
        if (expired(a, now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment expired");
        }

        Exam exam = examRepository.findById(a.getExamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));

        if (STATUS_ASSIGNED.equals(a.getStatus())) {
            a.setStatus(STATUS_IN_PROGRESS);
            examAssignmentRepository.save(a);
        }

        List<ExamQuestion> eqs = examQuestionRepository.findByExamIdOrderByPositionAsc(exam.getId());
        List<Long> qIds = eqs.stream().map(ExamQuestion::getQuestionId).toList();
        List<Question> questions = questionRepository.findAllById(qIds);

        SecureRandom rnd = new SecureRandom();
        List<ExamQuestionViewDTO> views = new ArrayList<>();

        for (ExamQuestion eq : eqs) {
            Question q = questions.stream().filter(qq -> qq.getId().equals(eq.getQuestionId())).findFirst().get();
            views.add(ExamQuestionViewDTO.builder()
                    .id(q.getId())
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .build());
        }

        Collections.shuffle(views, rnd);

        return TestSessionDTO.builder()
                .assignmentId(a.getId())
                .examId(exam.getId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .startedAt(now)
                .questions(views)
                .build();
    }

    private boolean expired(ExamAssignment a, LocalDateTime now) {
        return a.getEndTime() != null && now.isAfter(a.getEndTime());
    }

    private boolean canStartExam(ExamAssignment a, LocalDateTime now) {
        if (expired(a, now)) return false;
        if (!STATUS_ASSIGNED.equals(a.getStatus()) && !STATUS_IN_PROGRESS.equals(a.getStatus())) return false;
        if (a.getStartTime() != null && now.isBefore(a.getStartTime())) return false;
        return true;
    }

    private String getStatusMessage(ExamAssignment a, LocalDateTime now) {
        // Updated priority: completed > expired > others
        if (STATUS_COMPLETED.equals(a.getStatus())) return "Completed";
        if (expired(a, now)) return "Expired";
        if (a.getStartTime() != null && now.isBefore(a.getStartTime()))
            return "Available from " + a.getStartTime();
        return "Ready to start";
    }

    private void validateEmployees(List<Long> ids, Long companyId, String jwt) {
        List<UserSummaryDTO> employees = userClient.lookupEmployeesForCompany(jwt);

        Map<Long, UserSummaryDTO> map = employees.stream()
                .collect(Collectors.toMap(UserSummaryDTO::id, u -> u));

        for (Long id : ids) {
            if (!map.containsKey(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not in company: " + id);
            }
        }
    }
}
