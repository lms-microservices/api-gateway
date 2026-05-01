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
                .map(String::trim)
                .anyMatch(publicPath -> matches(publicPath, path));

        if (isConfiguredPublic) {
            return true;
        }

        return path.startsWith("/api/courses") && HttpMethod.GET.equals(method);
    }

    private boolean matches(String publicPath, String path) {
        if (publicPath.endsWith("/**")) {
            String prefix = publicPath.substring(0, publicPath.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }

        return path.equals(publicPath);
    }
}
