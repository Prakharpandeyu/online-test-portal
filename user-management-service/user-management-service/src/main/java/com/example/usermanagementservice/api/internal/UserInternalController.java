package com.example.usermanagementservice.api.internal;

import com.example.usermanagementservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserRepository userRepository;

    @GetMapping("/by-email")
    public ResponseEntity<UserDetailsDTO> getUserByEmail(@RequestParam String email) {
        var user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserDetailsDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isEnabled(),
                user.getCompanyId(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList())
        ));
    }

    public record UserDetailsDTO(
            Long id,
            String username,
            String email,
            String password,
            boolean enabled,
            Long companyId,
            java.util.List<String> roles
    ) {}
}
