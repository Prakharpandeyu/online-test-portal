package com.onlinetest.questionexam.controller;

import com.onlinetest.questionexam.integration.UserClient;
import com.onlinetest.questionexam.integration.UserClient.UserSummaryDTO;
import com.onlinetest.questionexam.dto.ApiResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final UserClient userClient;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<UserSummaryDTO>>> listEmployees(
            @RequestHeader("Authorization") String token) {

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

        List<UserSummaryDTO> employees = userClient.lookupEmployeesForCompany(jwt);

        return ResponseEntity.ok(
                ApiResponseDTO.success("Employees retrieved successfully", employees)
        );
    }
}
