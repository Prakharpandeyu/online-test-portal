package com.example.usermanagementservice.api.dto;

import java.util.List;

public record UserSummaryDTO(
        Long id, Long companyId, List<String> roles, String email, String name
) {}

