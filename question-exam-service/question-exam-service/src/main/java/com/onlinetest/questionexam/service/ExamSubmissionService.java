package com.onlinetest.questionexam.service;

import com.onlinetest.questionexam.dto.ExamResultDTO;
import com.onlinetest.questionexam.dto.ExamSubmitRequestDTO;
import com.onlinetest.questionexam.entity.*;
import com.onlinetest.questionexam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamSubmissionService {

    private final ExamRepository examRepository;
    private final ExamAssignmentRepository assignmentRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository attemptRepository;
    private final ExamAttemptAnswerRepository attemptAnswerRepository;

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_ASSIGNED = "ASSIGNED";

    public ExamResultDTO submitFinalAnswers(Long companyId, Long employeeId, ExamSubmitRequestDTO req) {
        // Load assignment
        ExamAssignment a = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        if (!Objects.equals(a.getCompanyId(), companyId) || !Objects.equals(a.getEmployeeId(), employeeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment not accessible");
        }
        if ("REVOKED".equals(a.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment revoked");
        }

        // Load exam
        Exam exam = examRepository.findById(req.getExamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
        if (!Objects.equals(exam.getCompanyId(), companyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam not found");
        }

        // Time window check
        LocalDateTime now = LocalDateTime.now();
        if (a.getStartTime() != null && now.isBefore(a.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment window not started");
        }
        if (a.getEndTime() != null && now.isAfter(a.getEndTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment window ended");
        }

        // Attempts check
        int attemptsUsed = getAttemptsUsed(a);
        if (attemptsUsed >= a.getMaxAttempts()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No attempts remaining");
        }

        // Load exam questions (ordered)
        List<ExamQuestion> eqs = examQuestionRepository.findByExamIdOrderByPositionAsc(exam.getId());
        if (eqs.size() != exam.getTotalQuestions()) {
            log.warn("Exam {} question count mismatch: expected {}, got {}", exam.getId(), exam.getTotalQuestions(), eqs.size());
        }
        Map<Long, Integer> questionIdToPosition = new HashMap<>();
        List<Long> qIds = new ArrayList<>(eqs.size());
        for (ExamQuestion eq : eqs) {
            qIds.add(eq.getQuestionId());
            questionIdToPosition.put(eq.getQuestionId(), eq.getPosition());
        }

        // Validate request answers against delivered set
        var answers = Optional.ofNullable(req.getAnswers()).orElse(List.of());
        Set<Long> uniqueQ = new HashSet<>();
        for (ExamSubmitRequestDTO.AnswerDTO ans : answers) {
            if (!questionIdToPosition.containsKey(ans.getQuestionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer includes non-exam question: " + ans.getQuestionId());
            }
            if (!uniqueQ.add(ans.getQuestionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate answer for question: " + ans.getQuestionId());
            }
            String s = ans.getSelected();
            if (!Set.of("A","B","C","D").contains(s)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid option: " + s);
            }
        }

        // Load question entities for correct answers
        List<Question> qs = questionRepository.findAllById(qIds);
        Map<Long, Question> qMap = qs.stream().collect(Collectors.toMap(Question::getId, x -> x));

        // Grade
        int total = eqs.size();
        int correct = 0;

        Map<Long, String> selectedMap = answers.stream()
                .collect(Collectors.toMap(ExamSubmitRequestDTO.AnswerDTO::getQuestionId, ExamSubmitRequestDTO.AnswerDTO::getSelected));

        List<ExamAttemptAnswer> persistedAnswers = new ArrayList<>(total);
        for (ExamQuestion eq : eqs) {
            Long qid = eq.getQuestionId();
            Question q = qMap.get(qid);
            String selected = selectedMap.getOrDefault(qid, null);
            boolean isCorrect = selected != null && q != null &&
                    q.getCorrectAnswer().name().equalsIgnoreCase(selected);
            if (isCorrect) correct++;

            ExamAttemptAnswer aaa = new ExamAttemptAnswer();
            // attemptId set after attempt is saved
            aaa.setQuestionId(qid);
            aaa.setSelected(selected != null ? Question.CorrectAnswer.valueOf(selected) : null);
            aaa.setIsCorrect(isCorrect);
            aaa.setPosition(eq.getPosition());
            persistedAnswers.add(aaa);
        }

        int percentage = total == 0 ? 0 : (int) Math.round(100.0 * correct / total);

        // Passing policy: per-exam passingPercentage (default 0 if null)
        int passPct = getPassingPercentage(exam);
        boolean passed = percentage >= passPct;

        // Duration policy
        int durationSeconds = clampDurationSeconds(req.getElapsedSeconds(), exam.getDurationMinutes());

        // Persist attempt then answers
        int nextAttemptNum = attemptsUsed + 1;
        ExamAttempt attempt = new ExamAttempt();
        attempt.setCompanyId(companyId);
        attempt.setExamId(exam.getId());
        attempt.setAssignmentId(a.getId());
        attempt.setEmployeeId(employeeId);
        attempt.setAttemptNumber(nextAttemptNum);
        attempt.setTotalQuestions(total);
        attempt.setCorrectAnswers(correct);
        attempt.setPercentage(percentage);
        attempt.setPassed(passed);
        attempt.setDurationSeconds(durationSeconds);
        attempt.setStatus("SUBMITTED");
        ExamAttempt savedAttempt = attemptRepository.save(attempt);

        for (ExamAttemptAnswer aaa : persistedAnswers) {
            aaa.setAttemptId(savedAttempt.getId());
        }
        attemptAnswerRepository.saveAll(persistedAnswers);

        // Update assignment status and attemptsUsed
        setAttemptsUsed(a, nextAttemptNum);
        if (passed) {
            a.setStatus(STATUS_COMPLETED);
        } else if (!STATUS_COMPLETED.equals(a.getStatus())) {
            // keep IN_PROGRESS or ASSIGNED to allow retry, within window
            if (!STATUS_IN_PROGRESS.equals(a.getStatus())) {
                a.setStatus(STATUS_ASSIGNED);
            }
        }
        assignmentRepository.save(a);

        int remaining = Math.max(0, a.getMaxAttempts() - nextAttemptNum);

        // Build response WITHOUT per-question correctness exposure
        return ExamResultDTO.builder()
                .attemptId(savedAttempt.getId())
                .attemptNumber(nextAttemptNum)
                .totalQuestions(total)
                .correctAnswers(correct)
                .percentage(percentage)
                .passed(passed)
                .durationSeconds(durationSeconds)
                .maxAttempts(a.getMaxAttempts())
                .attemptsUsed(nextAttemptNum)
                .attemptsRemaining(remaining)
                .submittedAt(savedAttempt.getCreatedDate())
                .questions(List.of()) // do not expose per-question correctness
                .build();
    }

    private int clampDurationSeconds(Integer elapsedSeconds, Integer durationMinutes) {
        int max = Math.max(1, Optional.ofNullable(durationMinutes).orElse(1)) * 60;
        int val = Optional.ofNullable(elapsedSeconds).orElse(max);
        if (val < 0) val = 0;
        return Math.min(val, max);
    }

    private int getPassingPercentage(Exam exam) {
        try {
            var field = exam.getClass().getDeclaredField("passingPercentage");
            field.setAccessible(true);
            Object v = field.get(exam);
            if (v instanceof Integer i) return i;
        } catch (Exception ignored) {}
        return 0; // default pass if policy not set
    }

    // attemptsUsed storage shim — will be persisted once column is added
    private int getAttemptsUsed(ExamAssignment a) {
        try {
            var f = a.getClass().getDeclaredField("attemptsUsed");
            f.setAccessible(true);
            Object v = f.get(a);
            if (v instanceof Integer i) return i;
        } catch (Exception ignored) {}
        // fallback: count attempts
        return (int) attemptRepository.countByAssignmentId(a.getId());
    }

    private void setAttemptsUsed(ExamAssignment a, int used) {
        try {
            var f = a.getClass().getDeclaredField("attemptsUsed");
            f.setAccessible(true);
            f.set(a, used);
        } catch (Exception ignored) {}
    }
}
