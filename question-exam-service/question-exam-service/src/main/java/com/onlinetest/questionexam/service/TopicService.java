package com.onlinetest.questionexam.service;

import com.onlinetest.questionexam.dto.TopicRequestDTO;
import com.onlinetest.questionexam.dto.TopicResponseDTO;
import com.onlinetest.questionexam.entity.Topic;
import com.onlinetest.questionexam.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of TopicService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TopicService  {

    private final TopicRepository topicRepository;


    public TopicResponseDTO createTopic(TopicRequestDTO requestDTO, Long companyId, Long userId, String role) {
        log.info("Creating topic: {} for company: {} by user: {} with role: {}",
                requestDTO.getName(), companyId, userId, role);

        // Check if topic name already exists
        if (topicRepository.existsByNameAndCompanyId(requestDTO.getName(), companyId)) {
            throw new RuntimeException("Topic with name '" + requestDTO.getName() + "' already exists");
        }

        // Create new topic with role (Updated constructor call)
        Topic topic = new Topic(companyId, requestDTO.getName(), requestDTO.getDescription(), userId, role);
        Topic savedTopic = topicRepository.save(topic);

        log.info("Topic created successfully with ID: {} by {}", savedTopic.getId(), role);
        return mapToResponseDTO(savedTopic);
    }


    @Transactional(readOnly = true)
    public List<TopicResponseDTO> getAllTopics(Long companyId) {
        log.info("Fetching topics for company: {}", companyId);

        List<Topic> topics = topicRepository.findByCompanyIdOrderByNameAsc(companyId);
        return topics.stream()
                .map(this::mapToResponseDTO) // Use stored role from database
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TopicResponseDTO> getTopicsWithPagination(Long companyId, Pageable pageable) {
        log.info("Fetching topics with pagination for company: {}, page: {}, size: {}",
                companyId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Topic> topicPage = topicRepository.findByCompanyId(companyId, pageable);

        return topicPage.map(this::mapToResponseDTO);
    }


    @Transactional(readOnly = true)
    public TopicResponseDTO getTopicById(Long topicId, Long companyId) {
        log.info("Fetching topic: {} for company: {}", topicId, companyId);

        Topic topic = topicRepository.findByIdAndCompanyId(topicId, companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found with ID: " + topicId));

        return mapToResponseDTO(topic); // Use stored role from database
    }


    public TopicResponseDTO updateTopic(Long topicId, TopicRequestDTO requestDTO, Long companyId) {
        log.info("Updating topic: {} for company: {}", topicId, companyId);

        Topic topic = topicRepository.findByIdAndCompanyId(topicId, companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found with ID: " + topicId));

        // Check if new name conflicts with existing topics
        if (!topic.getName().equals(requestDTO.getName()) &&
                topicRepository.existsByNameAndCompanyId(requestDTO.getName(), companyId)) {
            throw new RuntimeException("Topic with name '" + requestDTO.getName() + "' already exists");
        }

        // Update topic
        topic.setName(requestDTO.getName());
        topic.setDescription(requestDTO.getDescription());

        Topic updatedTopic = topicRepository.save(topic);
        log.info("Topic updated successfully: {}", updatedTopic.getId());

        return mapToResponseDTO(updatedTopic);
    }


    public void deleteTopic(Long topicId, Long companyId) {
        log.info("Deleting topic: {} for company: {}", topicId, companyId);

        Topic topic = topicRepository.findByIdAndCompanyId(topicId, companyId)
                .orElseThrow(() -> new RuntimeException("Topic not found with ID: " + topicId));

        topicRepository.delete(topic);
        log.info("Topic deleted successfully: {}", topicId);
    }

    @Transactional(readOnly = true)
    public List<TopicResponseDTO> searchTopics(Long companyId, String keyword) {
        log.info("Searching topics with keyword: {} for company: {}", keyword, companyId);

        List<Topic> topics = topicRepository.searchTopicsByName(companyId, keyword);
        return topics.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map Topic entity to TopicResponseDTO
     */
    private TopicResponseDTO mapToResponseDTO(Topic topic) {
        return TopicResponseDTO.builder()
                .id(topic.getId())
                .companyId(topic.getCompanyId())
                .name(topic.getName())
                .description(topic.getDescription())
                .createdByRole(topic.getCreatedByRole())
                .createdDate(topic.getCreatedDate())
                .updatedDate(topic.getUpdatedDate())
                .build();
    }
}
