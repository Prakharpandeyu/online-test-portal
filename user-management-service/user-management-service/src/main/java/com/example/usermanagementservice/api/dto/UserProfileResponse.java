package com.example.usermanagementservice.api.dto;

import com.example.usermanagementservice.user.Gender;

import java.time.LocalDate;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        Gender gender,
        Long companyId,
        String companyName,
        List<String> roles
) {}
