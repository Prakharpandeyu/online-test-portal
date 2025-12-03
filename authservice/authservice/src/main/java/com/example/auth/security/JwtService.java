package com.example.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private SecretKey keyFrom(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateAccessToken(String username, Collection<String> roles, Long companyId, Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getAccessToken().getExpirationMinutes(), ChronoUnit.MINUTES);

        var builder = Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setSubject(username)
                .setAudience("api")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("roles", roles)
                .claim("companyId", companyId);

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder
                .signWith(keyFrom(properties.getAccessToken().getSecret()), SignatureAlgorithm.HS256)
                .compact();
    }


    @Deprecated
    public String generateAccessTokenWithCompany(String username, Collection<String> roles, Long companyId) {
        return generateAccessToken(username, roles, companyId, null);
    }


    public String generateRefreshToken(String username, Long companyId, Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getRefreshToken().getExpirationDays(), ChronoUnit.DAYS);

        var builder = Jwts.builder()
                .setIssuer(properties.getIssuer())
                .setSubject(username)
                .setAudience("refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("companyId", companyId);

        if (userId != null) {
            builder.claim("userId", userId);
        }

        return builder
                .signWith(keyFrom(properties.getRefreshToken().getSecret()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyFrom(properties.getAccessToken().getSecret()))
                .build()
                .parseClaimsJws(token);
    }

    public Jws<Claims> parseRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyFrom(properties.getRefreshToken().getSecret()))
                .build()
                .parseClaimsJws(token);
    }
}
