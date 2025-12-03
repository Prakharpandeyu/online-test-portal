package com.example.auth.service;

import com.example.auth.api.dto.AuthResponse;
import com.example.auth.api.dto.LoginRequest;
import com.example.auth.integration.UserClient;
import com.example.auth.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserClient userClient;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserClient userClient) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userClient = userClient;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        var userDto = userClient.getUserByEmail(email);
        if (userDto == null) {
            throw new IllegalArgumentException("User not found");
        }

        List<String> roles = userDto.roles();
        Long companyId = userDto.companyId();
        Long userId = userDto.id();
        String subject = userDto.username();

        String accessToken = jwtService.generateAccessToken(subject, roles, companyId, userId);
        String refreshToken = jwtService.generateRefreshToken(subject, companyId, userId);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {

        var claims = jwtService.parseRefreshToken(refreshToken).getBody();
        String username = claims.getSubject();

        Long companyId = ((Number) claims.get("companyId")).longValue();
        Long userId = ((Number) claims.get("userId")).longValue();

        Object rolesObj = claims.get("roles");
        List<String> roles = rolesObj instanceof List<?> list
                ? list.stream().map(Object::toString).toList()
                : List.of();

        String newAccess = jwtService.generateAccessToken(username, roles, companyId, userId);
        return new AuthResponse(newAccess, refreshToken);
    }
}
