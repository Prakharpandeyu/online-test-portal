package com.onlinetest.questionexam.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponseDTO {

    private Long id;
    private Long companyId;
    private String name;
    private String description;
    private String createdByRole;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
