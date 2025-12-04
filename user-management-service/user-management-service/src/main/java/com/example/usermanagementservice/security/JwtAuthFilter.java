package com.example.usermanagementservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        log.error("JwtAuthFilter EXECUTED for URI = " + request.getRequestURI());

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            log.error("No Bearer token found");
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Validate token
            if (!jwtUtil.validateToken(token)) {
                log.error("Invalid JWT token");
                chain.doFilter(request, response);
                return;
            }

            // Extract details
            Long userId = jwtUtil.extractUserId(token);
            Long companyId = jwtUtil.extractCompanyId(token);
            String username = jwtUtil.extractUsername(token);
            String rolePlain = jwtUtil.extractRole(token); // ADMIN, SUPER_ADMIN, EMPLOYEE

            log.error("Parsed From JWT → userId=" + userId + ", companyId=" + companyId + ", username=" + username + ", role=" + rolePlain);

        
            var authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rolePlain)
            );

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            auth.setDetails(Map.of(
                    "userId", userId,
                    "companyId", companyId
            ));

            SecurityContextHolder.getContext().setAuthentication(auth);

            log.error("AUTH SET → " + auth.getDetails());

        } catch (Exception e) {
            log.error("JWT ERROR: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
