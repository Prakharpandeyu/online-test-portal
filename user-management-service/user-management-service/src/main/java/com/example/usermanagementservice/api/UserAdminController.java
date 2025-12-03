package com.example.usermanagementservice.api;

import com.example.usermanagementservice.api.dto.CreateUserRequest;
import com.example.usermanagementservice.api.dto.ResetPasswordRequest;
import com.example.usermanagementservice.service.UserService;
import com.example.usermanagementservice.user.User;
import com.example.usermanagementservice.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class UserAdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserAdminController(UserService userService,
                               UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/users/admin")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateUserRequest req,
                                         Authentication auth) {

        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        Long callerUserId = Long.valueOf(details.get("userId").toString());

        var caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));

        User admin = userService.createUser(req, "ROLE_ADMIN", caller.getCompany());

        return ResponseEntity.ok(Map.of(
                "message", "Admin user created",
                "userId", admin.getId()
        ));
    }

    @PostMapping("/users/employee")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<?> createEmployee(@Valid @RequestBody CreateUserRequest req,
                                            Authentication auth) {

        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        Long callerUserId = Long.valueOf(details.get("userId").toString());

        var caller = userRepository.findById(callerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));

        User emp = userService.createUser(req, "ROLE_EMPLOYEE", caller.getCompany());

        return ResponseEntity.ok(Map.of(
                "message", "Employee user created",
                "userId", emp.getId()
        ));
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody ResetPasswordRequest req,
                                           Authentication auth) {

        Map<String, Object> details = (Map<String, Object>) auth.getDetails();
        Long callerCompanyId = Long.valueOf(details.get("companyId").toString());

        String callerRole = auth.getAuthorities()
                .iterator().next().getAuthority()
                .replace("ROLE_", "");

        userService.resetPassword(id, callerCompanyId, callerRole, req.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "message", "Password reset successfully"
        ));
    }
}
