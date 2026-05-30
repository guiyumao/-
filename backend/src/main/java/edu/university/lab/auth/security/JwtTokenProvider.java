package edu.university.lab.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 令牌工具
 */
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;

    private final long expireMinutes;

    public JwtTokenProvider(
        @Value("${app.security.jwt-secret}") String secret,
        @Value("${app.security.expire-minutes}") long expireMinutes
    ) {
        byte[] keyBytes = secret.length() >= 32
            ? secret.getBytes(StandardCharsets.UTF_8)
            : Decoders.BASE64.decode("bGFiLW1hbmFnZW1lbnQtc3lzdGVtLWRlbW8tc2VjcmV0LWtleS0yMDI2");
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expireMinutes = expireMinutes;
    }

    public String createToken(LoginUser loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(loginUser.getUsername())
            .claim("uid", loginUser.getUser().getId())
            .claim("roles", loginUser.getRoleCodes())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireMinutes, ChronoUnit.MINUTES)))
            .signWith(secretKey)
            .compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
