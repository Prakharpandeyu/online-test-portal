package com.onlinetest.questionexam.repository;

import com.onlinetest.questionexam.entity.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findByCompanyIdOrderByNameAsc(Long companyId);

    Page<Topic> findByCompanyId(Long companyId, Pageable pageable);

    Optional<Topic> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);

    Optional<Topic> findByNameIgnoreCaseAndCompanyId(String name, Long companyId);

    @Query("SELECT t FROM Topic t WHERE t.companyId = :companyId AND " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY t.name ASC")
    List<Topic> searchTopicsByName(@Param("companyId") Long companyId,
                                   @Param("keyword") String keyword);

    long countByCompanyId(Long companyId);
}
