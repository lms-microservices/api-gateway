package com.lms.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicPathChecker {

    @Value("${jwt.public-paths}")
    private List<String> publicPaths;

    public boolean isPublic(String path, HttpMethod method) {
        boolean isConfiguredPublic = publicPaths.stream()
                .map(this::normalizePublicPathPattern)
                .anyMatch(path::startsWith);

        if (isConfiguredPublic) {
            return true;
        }

        return path.startsWith("/api/courses") && HttpMethod.GET.equals(method);
    }

    private String normalizePublicPathPattern(String publicPath) {
        return publicPath.replace("/**", "").trim();
    }
}
