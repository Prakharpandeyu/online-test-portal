package com.example.usermanagementservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JWTUtil {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.issuer:}")
    private String expectedIssuer;

    @Value("${security.jwt.audience:}")
    private String expectedAudience;

    public boolean validateToken(String token) {
        try {
            Jws<Claims> jws = parse(token);
            Claims c = jws.getBody();

            Date exp = c.getExpiration();
            if (exp != null && exp.before(new Date())) {
                log.warn("JWT expired at {}", exp);
                return false;
            }

            if (expectedIssuer != null && !expectedIssuer.isBlank()) {
                if (!expectedIssuer.equals(c.getIssuer())) {
                    log.warn("Issuer mismatch. expected={}, actual={}", expectedIssuer, c.getIssuer());
                    return false;
                }
            }

            Object audObj = c.get("aud");

            if (expectedAudience != null && !expectedAudience.isBlank()) {
                if (audObj instanceof String s) {
                    if (!s.equals(expectedAudience)) {
                        log.warn("Audience mismatch. expected={}, actual={}", expectedAudience, s);
                        return false;
                    }
                }

                else if (audObj instanceof List<?> list) {
                    if (!list.contains(expectedAudience)) {
                        log.warn("Audience list mismatch. expected={} not found", expectedAudience);
                        return false;
                    }
                }
                else if (audObj instanceof Set<?> set) {
                    if (!set.contains(expectedAudience)) {
                        log.warn("Audience set mismatch. expected={} not found", expectedAudience);
                        return false;
                    }
                }
                else {
                    log.warn("Unknown audience type: {}", audObj);
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Long extractUserId(String token) {
        Claims c = claims(token);
        Object v = c.get("userId");
        if (v == null) throw new IllegalArgumentException("userId missing in token");
        return toLong(v);
    }

    public Long extractCompanyId(String token) {
        Claims c = claims(token);
        Object v = c.get("companyId");
        if (v == null) throw new IllegalArgumentException("companyId missing in token");
        return toLong(v);
    }

    public String extractRole(String token) {
        Claims c = claims(token);

        Object rolesObj = c.get("roles");

        if (rolesObj instanceof List<?> list && !list.isEmpty()) {
            String r = String.valueOf(list.get(0));
            return r.startsWith("ROLE_") ? r.substring(5) : r;
        }

        Object roleObj = c.get("role");
        if (roleObj != null) {
            String r = String.valueOf(roleObj);
            return r.startsWith("ROLE_") ? r.substring(5) : r;
        }

        throw new IllegalArgumentException("role/roles missing in token");
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    private Claims claims(String token) {
        return parse(token).getBody();
    }

    private Jws<Claims> parse(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    private Long toLong(Object obj) {
        if (obj instanceof Integer i) return i.longValue();
        if (obj instanceof Long l) return l;
        if (obj instanceof String s) return Long.valueOf(s);
        throw new IllegalArgumentException("Cannot convert " + obj.getClass() + " to Long");
    }
}
