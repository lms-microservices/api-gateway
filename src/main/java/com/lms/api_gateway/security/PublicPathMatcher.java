package com.lms.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicPathMatcher {

    private final List<String> publicPaths;

    public PublicPathMatcher(@Value("${jwt.public-paths}") List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public boolean isPublic(String path, HttpMethod method) {
        boolean isConfiguredPublic = publicPaths.stream()
                .map(String::trim)
                .anyMatch(publicPath -> matches(publicPath, path));

        if (isConfiguredPublic) {
            return true;
        }

        boolean isPublicCourseGet = HttpMethod.GET.equals(method)
                && (path.equals("/api/courses")
                    || path.equals("/api/courses/search")
                    || path.matches("/api/courses/\\d+"));
        if (isPublicCourseGet) {
            return true;
        }

        boolean isPublicReviewGet = HttpMethod.GET.equals(method)
                && (path.equals("/api/reviews/courses")
                    || path.matches("/api/reviews/courses/\\d+")
                    || path.matches("/api/reviews/courses/\\d+/summary"));
        if (isPublicReviewGet) {
            return true;
        }

        boolean isPublicNotificationGet = HttpMethod.GET.equals(method)
                && (path.equals("/api/notifications")
                    || path.equals("/api/notifications/unread-count"));
        if (isPublicNotificationGet) {
            return true;
        }

        return false;
    }

    private boolean matches(String publicPath, String path) {
        if (publicPath.endsWith("/**")) {
            String prefix = publicPath.substring(0, publicPath.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }

        return path.equals(publicPath);
    }
}
