package com.example.auth.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    public UserDetailsDTO getUserByEmail(String email) {
        String url = userServiceBaseUrl + "/api/internal/users/by-email?email=" + email;
        try {
            ResponseEntity<UserDetailsDTO> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, UserDetailsDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to fetch user by email from User Management Service: {}", e.getMessage());
        }
        return null;
    }

    public record UserDetailsDTO(
            Long id,
            String username,
            String email,
            String password,
            boolean enabled,
            Long companyId,
            List<String> roles
    ) {}
}
