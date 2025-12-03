package com.onlinetest.questionexam.service;

import com.onlinetest.questionexam.dto.ExamCreateRequestDTO;
import com.onlinetest.questionexam.dto.ExamQuestionViewDTO;
import com.onlinetest.questionexam.dto.ExamResponseDTO;
import com.onlinetest.questionexam.entity.Exam;
import com.onlinetest.questionexam.entity.ExamQuestion;
import com.onlinetest.questionexam.entity.Question;
import com.onlinetest.questionexam.repository.ExamQuestionRepository;
import com.onlinetest.questionexam.repository.ExamRepository;
import com.onlinetest.questionexam.repository.QuestionRepository;
import com.onlinetest.questionexam.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExamService {

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final TopicRepository topicRepository;
    private final QuestionRepository questionRepository;

    // Create exam with manual per-topic question selection
    public ExamResponseDTO createExam(ExamCreateRequestDTO req, Long companyId, Long userId, String role) {

        log.info("Creating exam '{}' for company {} with topics payload", req.getTitle(), companyId);

        // Basic validations
        if (req.getDurationMinutes() == null || req.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }
        if (req.getTopics() == null || req.getTopics().isEmpty()) {
            throw new IllegalArgumentException("At least one topic is required");
        }

        int totalQuestions = 0;
        for (ExamCreateRequestDTO.ExamTopicRequestDTO t : req.getTopics()) {
            if (t.getTopicId() == null || t.getQuestionsCount() == null || t.getQuestionsCount() <= 0) {
                throw new IllegalArgumentException("Each topic must include topicId and questionsCount >= 1");
            }

            topicRepository.findByIdAndCompanyId(t.getTopicId(), companyId)
                    .orElseThrow(() -> new RuntimeException("Topic not found in company: " + t.getTopicId()));


            List<Long> activeIds = questionRepository.findActiveIdsByCompanyAndTopic(companyId, t.getTopicId());
            if (activeIds.size() < t.getQuestionsCount()) {
                throw new RuntimeException("Not enough active questions in topic: " + t.getTopicId());
            }
            totalQuestions += t.getQuestionsCount();
        }

        SecureRandom rnd = new SecureRandom();
        List<Long> chosen = new ArrayList<>(totalQuestions);

        for (ExamCreateRequestDTO.ExamTopicRequestDTO t : req.getTopics()) {
            List<Long> ids = questionRepository.findActiveIdsByCompanyAndTopic(companyId, t.getTopicId());
            Collections.shuffle(ids, rnd);
            chosen.addAll(ids.subList(0, t.getQuestionsCount()));
        }


        Collections.shuffle(chosen, rnd);

        // Persist exam
        Exam exam = new Exam();
        exam.setCompanyId(companyId);
        exam.setTitle(req.getTitle());
        exam.setDescription(req.getDescription());
        exam.setTotalQuestions(totalQuestions);
        exam.setDurationMinutes(req.getDurationMinutes());
        exam.setCreatedBy(userId);
        exam.setCreatedByRole(role);
        Exam saved = examRepository.save(exam);

        // Persist exam questions in order
        int pos = 1;
        for (Long qid : chosen) {
            ExamQuestion eq = new ExamQuestion();
            eq.setExamId(saved.getId());
            eq.setQuestionId(qid);
            eq.setPosition(pos++);
            examQuestionRepository.save(eq);
        }

        return ExamResponseDTO.builder()
                .id(saved.getId())
                .companyId(companyId)
                .title(saved.getTitle())
                .description(saved.getDescription())
                .totalQuestions(saved.getTotalQuestions())
                .durationMinutes(saved.getDurationMinutes())
                .selectedTopicCount(req.getTopics().size())
                .createdDate(saved.getCreatedDate())
                .build();
    }

    // Read: fetch exam details for the same tenant, including computed selectedTopicCount
    @Transactional(readOnly = true)
    public ExamResponseDTO getExamById(Long examId, Long companyId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found: " + examId));
        if (!exam.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Exam not found: " + examId);
        }

        var eqs = examQuestionRepository.findByExamIdOrderByPositionAsc(examId);
        var qIds = eqs.stream().map(ExamQuestion::getQuestionId).toList();

        int topicCount = 0;
        if (!qIds.isEmpty()) {
            Set<Long> distinctTopics = questionRepository.findAllById(qIds).stream()
                    .map(Question::getTopicId)
                    .collect(Collectors.toSet());
            topicCount = distinctTopics.size();
        }

        return ExamResponseDTO.builder()
                .id(exam.getId())
                .companyId(exam.getCompanyId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .durationMinutes(exam.getDurationMinutes())
                .selectedTopicCount(topicCount)
                .createdDate(exam.getCreatedDate())
                .build();
    }

    // Read: deliver all questions and options (no correct answers) in persisted order
    @Transactional(readOnly = true)
    public List<ExamQuestionViewDTO> getExamQuestionsForDelivery(Long examId, Long companyId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found: " + examId));
        if (!exam.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Exam not found: " + examId);
        }

        var eqs = examQuestionRepository.findByExamIdOrderByPositionAsc(examId);
        var qIds = eqs.stream().map(ExamQuestion::getQuestionId).toList();

        // Bulk fetch questions, then map in the same order
        List<Question> questions = questionRepository.findAllById(qIds);

        return eqs.stream().map(eq -> {
            Question q = questions.stream()
                    .filter(qq -> qq.getId().equals(eq.getQuestionId()))
                    .findFirst().orElse(null);
            if (q == null) return null;
            return ExamQuestionViewDTO.builder()
                    .id(q.getId())
                    .topicId(q.getTopicId())
                    .topicName(q.getTopic() != null ? q.getTopic().getName() : null)
                    .questionText(q.getQuestionText())
                    .optionA(q.getOptionA())
                    .optionB(q.getOptionB())
                    .optionC(q.getOptionC())
                    .optionD(q.getOptionD())
                    .build();
        }).filter(Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public List<ExamResponseDTO> listCompanyExams(Long companyId) {
        return examRepository.findAll().stream()
                .filter(e -> e.getCompanyId().equals(companyId))
                .map(e -> ExamResponseDTO.builder()
                        .id(e.getId())
                        .companyId(e.getCompanyId())
                        .title(e.getTitle())
                        .description(e.getDescription())
                        .totalQuestions(e.getTotalQuestions())
                        .durationMinutes(e.getDurationMinutes())
                        .createdDate(e.getCreatedDate())
                        .build())
                .toList();
    }

    // Update exam by id (metadata only; question set remains unchanged unless you add logic)
    public ExamResponseDTO updateExam(Long examId, ExamCreateRequestDTO req, Long companyId, Long userId, String role) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found: " + examId));
        if (!exam.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Exam not found: " + examId);
        }

        exam.setTitle(req.getTitle());
        exam.setDescription(req.getDescription());
        // Do not trust inbound totalQuestions; keep existing or recompute if needed
        exam.setDurationMinutes(req.getDurationMinutes());
        exam.setUpdatedBy(userId);
        exam.setUpdatedByRole(role);
        examRepository.save(exam);

        return ExamResponseDTO.builder()
                .id(exam.getId())
                .companyId(exam.getCompanyId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .durationMinutes(exam.getDurationMinutes())
                .createdDate(exam.getCreatedDate())
                .build();
    }

    // Delete exam by id
    public void deleteExam(Long examId, Long companyId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found: " + examId));
        if (!exam.getCompanyId().equals(companyId)) {
            throw new RuntimeException("Exam not found: " + examId);
        }

        // Delete associated exam questions first
        examQuestionRepository.deleteByExamId(examId);

        examRepository.delete(exam);
    }
}
