package com.onlinetest.questionexam.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @Value("${services.user.base-url:http://localhost:8082}")
    private String userServiceBaseUrl;

    // employees only
    public List<UserSummaryDTO> lookupEmployeesForCompany(String bearerToken) {
        String url = userServiceBaseUrl + "/api/v1/users/employees/company/me";
        return doGetList(url, bearerToken);
    }

    // all users
    public List<UserSummaryDTO> lookupAllUsersForCompany(String bearerToken) {
        String url = userServiceBaseUrl + "/api/v1/users/company/me";
        return doGetList(url, bearerToken);
    }

    private List<UserSummaryDTO> doGetList(String url, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.replace("Bearer ", ""));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserSummaryDTO[]> resp = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserSummaryDTO[].class
        );

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new RuntimeException("Failed to fetch users from User Service: " + resp.getStatusCode());
        }

        return List.of(Objects.requireNonNull(resp.getBody()));
    }

    public record UserSummaryDTO(
            Long id,
            Long companyId,
            java.util.List<String> roles,
            String email,
            String name
    ) {}
}
