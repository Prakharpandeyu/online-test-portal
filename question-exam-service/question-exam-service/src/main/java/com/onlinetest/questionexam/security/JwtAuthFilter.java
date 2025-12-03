package com.onlinetest.questionexam.security;

import com.onlinetest.questionexam.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token for request: {}", request.getRequestURI());
                chain.doFilter(request, response);
                return;
            }

            Long userId = jwtUtil.extractUserId(token);
            Long companyId = jwtUtil.extractCompanyId(token);
            String rolePlain = jwtUtil.extractRole(token); // SUPER_ADMIN / ADMIN / EMPLOYEE

            // Ensure role always starts with "ROLE_"
            String roleWithPrefix = rolePlain.startsWith("ROLE_") ? rolePlain : "ROLE_" + rolePlain;
            var authorities = List.of(new SimpleGrantedAuthority(roleWithPrefix));

            var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
            auth.setDetails(companyId);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Log successful authentication (INFO for visibility)
            log.info("Authenticated userId={}, companyId={}, roles={}", userId, companyId, authorities);

        } catch (Exception e) {
            log.error("JWT processing failed for request {}: {}", request.getRequestURI(), e.getMessage());
        }

        chain.doFilter(request, response);
    }
}
