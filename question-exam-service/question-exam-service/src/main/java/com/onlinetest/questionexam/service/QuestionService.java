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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    public QuestionResponseDTO createQuestion(QuestionRequestDTO requestDTO, Long companyId, Long userId, String role) {

        Topic topic = topicRepository.findByIdAndCompanyId(requestDTO.getTopicId(), companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + requestDTO.getTopicId()));

        if (questionRepository.existsByCompanyIdAndQuestionTextIgnoreCase(companyId, requestDTO.getQuestionText())) {
            throw new RuntimeException("Question already exists");
        }

        Question q = new Question();
        q.setCompanyId(companyId);
        q.setTopicId(topic.getId());
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
        return mapToDTO(saved, topic.getName());
    }

    public List<QuestionResponseDTO> uploadQuestionsCsv(MultipartFile file, Long companyId, Long userId, String role) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Empty file");
        }
        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Only CSV supported");
        }

        List<QuestionResponseDTO> results = new ArrayList<>();

        try (Reader in = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {

            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreEmptyLines()
                    .withTrim(true)
                    .parse(in);

            // Normalize headers to remove BOM
            Map<String, Integer> cleanedHeaders = new LinkedHashMap<>();
            parser.getHeaderMap().forEach((key, val) -> {
                String cleaned = key.replace("\uFEFF", "").trim();
                cleanedHeaders.put(cleaned, val);
            });

            // Reparse using cleaned headers
            parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT
                            .withHeader(cleanedHeaders.keySet().toArray(new String[0]))
                            .withSkipHeaderRecord()
                            .withIgnoreEmptyLines()
                            .withTrim(true)
            );

            requireHeaders(cleanedHeaders,
                    "topicName", "questionText", "optionA", "optionB", "optionC", "optionD", "correctAnswer");

            for (CSVRecord rec : parser) {
                try {
                    String topicName = nonEmpty(rec.get("topicName"), "topicName");
                    String questionText = nonEmpty(rec.get("questionText"), "questionText");
                    String optionA = nonEmpty(rec.get("optionA"), "optionA");
                    String optionB = nonEmpty(rec.get("optionB"), "optionB");
                    String optionC = nonEmpty(rec.get("optionC"), "optionC");
                    String optionD = nonEmpty(rec.get("optionD"), "optionD");

                    String correctVal = nonEmpty(rec.get("correctAnswer"), "correctAnswer")
                            .trim().toUpperCase();

                    Question.CorrectAnswer correctAnswer =
                            Question.CorrectAnswer.valueOf(correctVal);

                    Topic topic = topicRepository.findByNameIgnoreCaseAndCompanyId(topicName, companyId)
                            .orElseThrow(() ->
                                    new RuntimeException("Topic not found for company: " + topicName)
                            );

                    if (questionRepository.existsByCompanyIdAndQuestionTextIgnoreCase(companyId, questionText)) {
                        log.warn("Skipping duplicate: {}", questionText);
                        continue;
                    }

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

                } catch (Exception ex) {
                    log.error("CSV row {} error: {}", rec.getRecordNumber(), ex.getMessage());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("CSV read failed: " + e.getMessage());
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getAllQuestions(Long companyId) {
        return questionRepository.findByCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(companyId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionResponseDTO getQuestionById(Long questionId, Long companyId) {
        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        return mapToDTO(q);
    }

    public QuestionResponseDTO updateQuestion(Long questionId, QuestionRequestDTO requestDTO, Long companyId) {

        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Topic topic = topicRepository.findByIdAndCompanyId(requestDTO.getTopicId(), companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        q.setTopicId(topic.getId());
        q.setQuestionText(requestDTO.getQuestionText());
        q.setOptionA(requestDTO.getOptionA());
        q.setOptionB(requestDTO.getOptionB());
        q.setOptionC(requestDTO.getOptionC());
        q.setOptionD(requestDTO.getOptionD());
        q.setCorrectAnswer(requestDTO.getCorrectAnswer());

        Question updated = questionRepository.save(q);
        return mapToDTO(updated);
    }

    public void deleteQuestion(Long questionId, Long companyId) {
        Question q = questionRepository.findByIdAndCompanyId(questionId, companyId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        q.setIsActive(false);
        questionRepository.save(q);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getQuestionsByTopic(Long topicId, Long companyId) {
        return questionRepository.findByTopicIdAndCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(topicId, companyId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Helpers
    private void requireHeaders(Map<String, Integer> headers, String... needed) {
        Set<String> normalized = headers.keySet().stream()
                .map(h -> h.replace("\uFEFF", "").trim().toLowerCase())
                .collect(Collectors.toSet());

        for (String need : needed) {
            if (!normalized.contains(need.toLowerCase())) {
                throw new RuntimeException("Missing header: " + need);
            }
        }
    }

    private String nonEmpty(String val, String field) {
        if (val == null || val.trim().isEmpty()) {
            throw new RuntimeException("Missing: " + field);
        }
        return val.trim();
    }

    private QuestionResponseDTO mapToDTO(Question q) {
        String topicName = topicRepository.findById(q.getTopicId())
                .map(Topic::getName).orElse("Unknown");

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
