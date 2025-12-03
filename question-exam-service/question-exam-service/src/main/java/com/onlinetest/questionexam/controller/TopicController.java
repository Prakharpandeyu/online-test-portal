package com.onlinetest.questionexam.controller;

import com.onlinetest.questionexam.dto.ApiResponseDTO;
import com.onlinetest.questionexam.dto.PaginatedResponseDTO;
import com.onlinetest.questionexam.dto.TopicRequestDTO;
import com.onlinetest.questionexam.dto.TopicResponseDTO;
import com.onlinetest.questionexam.service.TopicService;  // ← ADD THIS IMPORT
import com.onlinetest.questionexam.util.JWTUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Topic management operations
 * Handles CRUD operations for topics with company-specific isolation
 */
@RestController
@RequestMapping("/api/v1/topics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TopicController {

    private final TopicService topicService;
    private final JWTUtil jwtUtil;

    /**
     * Create a new topic
     * Only SUPER_ADMIN and ADMIN can create topics
     */
    @PostMapping
    public ResponseEntity<ApiResponseDTO<TopicResponseDTO>> createTopic(
            @Valid @RequestBody TopicRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {

        log.info("Creating new topic: {}", requestDTO.getName());

        // Extract JWT token (remove "Bearer " prefix)
        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);
        Long userId = jwtUtil.extractUserId(jwtToken);
        String role = jwtUtil.extractRole(jwtToken);

        log.info("Creating topic for company: {}, userId: {}, role: {}", companyId, userId, role);

        TopicResponseDTO responseDTO = topicService.createTopic(requestDTO, companyId, userId, role);

        ApiResponseDTO<TopicResponseDTO> response = ApiResponseDTO.success(
                "Topic created successfully", responseDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all topics for the company
     */
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TopicResponseDTO>>> getAllTopics(
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        List<TopicResponseDTO> topics = topicService.getAllTopics(companyId);

        ApiResponseDTO<List<TopicResponseDTO>> response = ApiResponseDTO.success(
                "Topics retrieved successfully", topics);

        return ResponseEntity.ok(response);
    }

    /**
     * Get topics with pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponseDTO<PaginatedResponseDTO<TopicResponseDTO>>> getTopicsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        Pageable pageable = PageRequest.of(page, size);
        Page<TopicResponseDTO> topicPage = topicService.getTopicsWithPagination(companyId, pageable);

        // Convert to simple pagination response
        PaginatedResponseDTO<TopicResponseDTO> simpleResponse = PaginatedResponseDTO.<TopicResponseDTO>builder()
                .content(topicPage.getContent())
                .currentPage(topicPage.getNumber())
                .pageSize(topicPage.getSize())
                .totalPages(topicPage.getTotalPages())
                .totalElements(topicPage.getTotalElements())
                .hasNext(topicPage.hasNext())
                .hasPrevious(topicPage.hasPrevious())
                .build();

        ApiResponseDTO<PaginatedResponseDTO<TopicResponseDTO>> response = ApiResponseDTO.success(
                "Topics retrieved successfully", simpleResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get topic by ID
     */
    @GetMapping("/{topicId}")
    public ResponseEntity<ApiResponseDTO<TopicResponseDTO>> getTopicById(
            @PathVariable Long topicId,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        TopicResponseDTO topic = topicService.getTopicById(topicId, companyId);

        ApiResponseDTO<TopicResponseDTO> response = ApiResponseDTO.success(
                "Topic retrieved successfully", topic);

        return ResponseEntity.ok(response);
    }

    /**
     * Update topic
     * Only SUPER_ADMIN and ADMIN can update topics
     */
    @PutMapping("/{topicId}")
    public ResponseEntity<ApiResponseDTO<TopicResponseDTO>> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody TopicRequestDTO requestDTO,
            @RequestHeader("Authorization") String token) {

        log.info("Updating topic with ID: {}", topicId);

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        TopicResponseDTO responseDTO = topicService.updateTopic(topicId, requestDTO, companyId);

        ApiResponseDTO<TopicResponseDTO> response = ApiResponseDTO.success(
                "Topic updated successfully", responseDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete topic
     * Only SUPER_ADMIN and ADMIN can delete topics
     */
    @DeleteMapping("/{topicId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteTopic(
            @PathVariable Long topicId,
            @RequestHeader("Authorization") String token) {

        log.info("Deleting topic with ID: {}", topicId);

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        topicService.deleteTopic(topicId, companyId);

        ApiResponseDTO<Void> response = ApiResponseDTO.success(
                "Topic deleted successfully", null);

        return ResponseEntity.ok(response);
    }

    /**
     * Search topics by name
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<TopicResponseDTO>>> searchTopics(
            @RequestParam String keyword,
            @RequestHeader("Authorization") String token) {

        String jwtToken = token.substring(7);
        Long companyId = jwtUtil.extractCompanyId(jwtToken);

        List<TopicResponseDTO> topics = topicService.searchTopics(companyId, keyword);

        ApiResponseDTO<List<TopicResponseDTO>> response = ApiResponseDTO.success(
                "Search results retrieved successfully", topics);

        return ResponseEntity.ok(response);
    }
}
