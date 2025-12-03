package com.example.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String issuer;
    private Token accessToken = new Token();
    private Token refreshToken = new Token();

    public static class Token {
        private String secret;
        private int expirationMinutes;
        private int expirationDays;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { this.expirationMinutes = expirationMinutes; }
        public int getExpirationDays() { return expirationDays; }
        public void setExpirationDays(int expirationDays) { this.expirationDays = expirationDays; }
    }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public Token getAccessToken() { return accessToken; }
    public Token getRefreshToken() { return refreshToken; }
}
