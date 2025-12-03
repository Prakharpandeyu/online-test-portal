package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(Long companyId);

    Optional<Question> findByIdAndCompanyId(Long id, Long companyId);

    List<Question> findByTopicIdAndCompanyIdAndIsActiveTrueOrderByCreatedDateDesc(Long topicId, Long companyId);

    boolean existsByCompanyIdAndQuestionTextIgnoreCase(Long companyId, String questionText);

    long countByTopicIdAndCompanyIdAndIsActiveTrue(Long topicId, Long companyId);

    @Query("select q.id from Question q where q.companyId = :companyId and q.isActive = true and q.topicId = :topicId")
    List<Long> findActiveIdsByCompanyAndTopic(@Param("companyId") Long companyId, @Param("topicId") Long topicId);
}
