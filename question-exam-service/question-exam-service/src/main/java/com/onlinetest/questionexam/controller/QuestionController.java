package com.onlinetest.questionexam.controller;

import com.onlinetest.questionexam.dto.ApiResponseDTO;
import com.onlinetest.questionexam.dto.QuestionRequestDTO;
import com.onlinetest.questionexam.dto.QuestionResponseDTO;
import com.onlinetest.questionexam.service.QuestionService;
import com.onlinetest.questionexam.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;
    private final JWTUtil jwtUtil;

    //create new question only super admin/admins
    @PostMapping
    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> createQuestion(
            @Valid @RequestBody QuestionRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {

        log.info("Creating new question for topic: {}", requestDTO.getTopicId());

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);
        Long userId = jwtUtil.extractUserId(jwtToken);
        String role = jwtUtil.extractRole(jwtToken);

        QuestionResponseDTO responseDTO = questionService.createQuestion(requestDTO, companyId, userId, role);

        ApiResponseDTO<QuestionResponseDTO> response = ApiResponseDTO.success(
                "Question created successfully", responseDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/csv", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> uploadQuestionsCsv(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {

        log.info("Uploading questions CSV: {}", file != null ? file.getOriginalFilename() : "null");

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);
        Long userId = jwtUtil.extractUserId(jwtToken);
        String role = jwtUtil.extractRole(jwtToken);

        List<QuestionResponseDTO> saved = questionService.uploadQuestionsCsv(file, companyId, userId, role);

        ApiResponseDTO<List<QuestionResponseDTO>> response = ApiResponseDTO.success(
                "CSV processed successfully: " + saved.size() + " questions created", saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getAllQuestions(
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        List<QuestionResponseDTO> questions = questionService.getAllQuestions(companyId);

        ApiResponseDTO<List<QuestionResponseDTO>> response = ApiResponseDTO.success(
                "Questions retrieved successfully", questions);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> getQuestionById(
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        QuestionResponseDTO question = questionService.getQuestionById(questionId, companyId);

        ApiResponseDTO<QuestionResponseDTO> response = ApiResponseDTO.success(
                "Question retrieved successfully", question);

        return ResponseEntity.ok(response);
    }

   //update question only super admin/admin
    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {

        log.info("Updating question with ID: {}", questionId);

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        QuestionResponseDTO responseDTO = questionService.updateQuestion(questionId, requestDTO, companyId);

        ApiResponseDTO<QuestionResponseDTO> response = ApiResponseDTO.success(
                "Question updated successfully", responseDTO);

        return ResponseEntity.ok(response);
    }

    //only super admin/admin
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteQuestion(
            @PathVariable Long questionId,
            @RequestHeader("Authorization") String token) {

        log.info("Deleting question with ID: {}", questionId);

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        questionService.deleteQuestion(questionId, companyId);

        ApiResponseDTO<Void> response = ApiResponseDTO.success(
                "Question deleted successfully", null);

        return ResponseEntity.ok(response);
    }

   //get question by topic
    @GetMapping("/topic/{topicId}")
    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getQuestionsByTopic(
            @PathVariable Long topicId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        List<QuestionResponseDTO> questions = questionService.getQuestionsByTopic(topicId, companyId);

        ApiResponseDTO<List<QuestionResponseDTO>> response = ApiResponseDTO.success(
                "Questions retrieved successfully", questions);

        return ResponseEntity.ok(response);
    }
}
