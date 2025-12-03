package com.onlinetest.questionexam.service;

import com.onlinetest.questionexam.dto.QuestionRequestDTO;
import com.onlinetest.questionexam.dto.QuestionResponseDTO;
import com.onlinetest.questionexam.entity.Question;
import com.onlinetest.questionexam.entity.Topic;
import com.onlinetest.questionexam.repository.QuestionRepository;
import com.onlinetest.questionexam.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Question Service - Direct implementation (no interface)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    /**
     * Create a new question
     */
    public QuestionResponseDTO createQuestion(QuestionRequestDTO requestDTO, Long companyId, Long userId, String role) {
        log.info("Creating question for topic: {} in company: {} by user: {} with role: {}",
                requestDTO.getTopicId(), companyId, userId, role);

        // Validate topic belongs to the company
        Topic topic = topicRepository.findByIdAndCompanyId(requestDTO.getTopicId(), companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found with ID: " + requestDTO.getTopicId()));

        // Prevent duplicates
        if (questionRepository.existsByCompanyIdAndQuestionTextIgnoreCase(companyId, requestDTO.getQuestionText())) {
            throw new RuntimeException("Question with similar text already exists");
        }

        // Build and save
        Question q = new Question();
        q.setCompanyId(companyId);
        q.setTopicId(requestDTO.getTopicId());
        q.setQuestionText(requestDTO.getQuestionText());
        q.setOptionA(requestDTO.getOptionA());
        q.setOptionB(requestDTO.getOptionB());
        q.setOptionC(requestDTO.getOptionC());
        q.setOptionD(requestDTO.getOptionD());
        q.setCorrectAnswer(requestDTO.getCorrectAnswer());
        q.setCreatedBy(userId);
        q.setCreatedByRole(role);
        q.setIsActive(true);

        Question saved = questionRepository.save(q);
        log.info("Question created: {}", saved.getId());

        return mapToDTO(saved, topic.getName());
    }

    /**
     * Bulk upload questions via CSV
     */
    public List<QuestionResponseDTO> uploadQuestionsCsv(MultipartFile file, Long companyId, Long userId, String role) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }
        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Only CSV files are supported");
        }

        List<QuestionResponseDTO> results = new ArrayList<>();

        try (Reader in = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            CSVFormat format = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreEmptyLines()
                    .withTrim(true)
                    .withIgnoreSurroundingSpaces(true);

            CSVParser parser = format.parse(in);

            // Required headers
            requireHeaders(parser.getHeaderMap(),
                    "topicName", "questionText", "optionA", "optionB", "optionC", "optionD", "correctAnswer");

            for (CSVRecord rec : parser) {
                try {
                    String topicName = nonEmpty(rec.get("topicName"), "topicName");
                    String questionText = nonEmpty(rec.get("questionText"), "questionText");
                    String optionA = nonEmpty(rec.get("optionA"), "optionA");
                    String optionB = nonEmpty(rec.get("optionB"), "optionB");
                    String optionC = nonEmpty(rec.get("optionC"), "optionC");
                    String optionD = nonEmpty(rec.get("optionD"), "optionD");

                    String correct = nonEmpty(rec.get("correctAnswer"), "correctAnswer")
                            .trim().toUpperCase();

                    Question.CorrectAnswer correctAnswer =
                            Question.CorrectAnswer.valueOf(correct);

                    // Find topic by NAME + COMPANY
                    Topic topic = topicRepository
                            .findByNameIgnoreCaseAndCompanyId(topicName, companyId)
                            .orElseThrow(() ->
                                    new RuntimeException("Topic not found for company: " + topicName));

                    // Skip duplicates
                    if (questionRepository.existsByCompanyIdAndQuestionTextIgnoreCase(companyId, questionText)) {
                        log.warn("Duplicate skipped (questionText): {}", questionText);
                        continue;
                    }

                    // Build & save
                    Question q = new Question();
                    q.setCompanyId(companyId);
                    q.setTopicId(topic.getId());
                    q.setQuestionText(questionText);
                    q.setOptionA(optionA);
                    q.setOptionB(optionB);
                    q.setOptionC(optionC);
                    q.setOptionD(optionD);
                    q.setCorrectAnswer(correctAnswer);
                    q.setCreatedBy(userId);
                    q.setCreatedByRole(role);
                    q.setIsActive(true);

                    Question saved = questionRepository.save(q);
                    results.add(mapToDTO(saved, topic.getName()));

                } catch (Exception rowEx) {
                    log.error("CSV row {} error: {}", rec.getRecordNumber(), rowEx.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + e.getMessage(), e);
        }

        return results;
    }

    /**
     * Get all questions for a company
     */
    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getAllQuestions(Long companyId) {
        List<Question> list = questionRepository.findByCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(companyId);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Get question by ID
     */
    @Transactional(readOnly = true)
    public QuestionResponseDTO getQuestionById(Long questionId, Long companyId) {
        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));
        return mapToDTO(q);
    }

    /**
     * Update question
     */
    public QuestionResponseDTO updateQuestion(Long questionId, QuestionRequestDTO requestDTO, Long companyId) {
        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        topicRepository.findByIdAndCompanyId(requestDTO.getTopicId(), companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found with ID: " + requestDTO.getTopicId()));

        if (!q.getQuestionText().equalsIgnoreCase(requestDTO.getQuestionText())
                && questionRepository.existsByCompanyIdAndQuestionTextIgnoreCase(companyId, requestDTO.getQuestionText())) {
            throw new RuntimeException("Question with similar text already exists");
        }

        q.setTopicId(requestDTO.getTopicId());
        q.setQuestionText(requestDTO.getQuestionText());
        q.setOptionA(requestDTO.getOptionA());
        q.setOptionB(requestDTO.getOptionB());
        q.setOptionC(requestDTO.getOptionC());
        q.setOptionD(requestDTO.getOptionD());
        q.setCorrectAnswer(requestDTO.getCorrectAnswer());

        Question updated = questionRepository.save(q);
        return mapToDTO(updated);
    }

    /**
     * Soft delete
     */
    public void deleteQuestion(Long questionId, Long companyId) {
        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));
        q.setIsActive(false);
        questionRepository.save(q);
        log.info("Question soft-deleted: {}", questionId);
    }

    /**
     * Get questions by topic
     */
    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getQuestionsByTopic(Long topicId, Long companyId) {
        List<Question> list = questionRepository.findByTopicIdAndCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(topicId, companyId);
        return list.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ---- Helpers ----

    private void requireHeaders(Map<String, Integer> headerMap, String... names) {
        for (String n : names) {
            boolean present = headerMap.keySet().stream().anyMatch(h -> h.equalsIgnoreCase(n));
            if (!present) {
                throw new RuntimeException("Missing required header: " + n);
            }
        }
    }

    private String nonEmpty(String val, String field) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing value for " + field);
        }
        return val.trim();
    }

    private Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid number: " + s);
        }
    }

    private QuestionResponseDTO mapToDTO(Question q) {
        String topicName = (q.getTopic() != null)
                ? q.getTopic().getName()
                : topicRepository.findById(q.getTopicId()).map(Topic::getName).orElse("Unknown");
        return mapToDTO(q, topicName);
    }

    private QuestionResponseDTO mapToDTO(Question q, String topicName) {
        return QuestionResponseDTO.builder()
                .id(q.getId())
                .companyId(q.getCompanyId())
                .topicId(q.getTopicId())
                .topicName(topicName)
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .correctAnswer(q.getCorrectAnswer())
                .createdByRole(q.getCreatedByRole())
                .isActive(q.getIsActive())
                .createdDate(q.getCreatedDate())
                .updatedDate(q.getUpdatedDate())
                .build();
    }
}
