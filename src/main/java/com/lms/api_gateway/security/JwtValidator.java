package com.lms.api_gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public Claims validateAndParse(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);

        if (value != null) {
            return value.toString();
        }

        if ("userId".equals(claimName)) {
            return claims.getSubject();
        }

        return null;
    }

    public List<String> getStringListClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);

        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }

        return List.of();
    }
}
