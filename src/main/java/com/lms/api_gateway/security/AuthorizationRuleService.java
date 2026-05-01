package com.lms.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class AuthorizationRuleService {
    private final List<AccessRule> rules = List.of(
            rule(HttpMethod.GET, "/api/auth/roles", "roles:manage", false),
            rule(HttpMethod.POST, "/api/auth/roles", "roles:manage", false),
            rule(HttpMethod.GET, "^/api/auth/roles/permissions$", "roles:manage", false),
            rule(HttpMethod.POST, "^/api/auth/roles/permissions$", "roles:manage", false),
            rule(HttpMethod.GET, "^/api/auth/roles/[^/]+$", "roles:manage", false),
            rule(HttpMethod.DELETE, "^/api/auth/roles/[^/]+$", "roles:manage", false),
            rule(HttpMethod.POST, "^/api/auth/roles/[^/]+/permissions$", "roles:manage", false),
            rule(HttpMethod.DELETE, "^/api/auth/roles/[^/]+/permissions/[^/]+$", "roles:manage", false),
            rule(HttpMethod.PUT, "^/api/auth/users/[^/]+/role$", "roles:manage", false),
            rule(HttpMethod.DELETE, "^/api/auth/users/([^/]+)$", "users:delete", false),

            rule(HttpMethod.GET, "/api/users", "users:read", false),
            rule(HttpMethod.GET, "^/api/users/email/[^/]+$", "users:read", false),
            rule(HttpMethod.PUT, "^/api/users/([^/]+)/approve-instructor$", "users:update", false),
            rule(HttpMethod.GET, "^/api/users/([^/]+)$", "users:read", true),
            rule(HttpMethod.PUT, "^/api/users/([^/]+)$", "users:update", true),

            rule(HttpMethod.POST, "^/api/courses.*$", "courses:create", false),
            rule(HttpMethod.PUT, "^/api/courses.*$", "courses:update", false),
            rule(HttpMethod.DELETE, "^/api/courses.*$", "courses:update", false)
    );

    public boolean isAuthorized(String path, HttpMethod method, String userId, List<String> permissions) {
        return rules.stream()
                .filter(r -> r.matches(path, method))
                .findFirst()
                .map(r -> hasPermission(r, permissions) || isSelf(r, path, userId))
                .orElse(true);
    }

    private boolean hasPermission(AccessRule rule, List<String> permissions) {
        return rule.requiredPermission == null || permissions.contains(rule.requiredPermission);
    }

    private boolean isSelf(AccessRule rule, String path, String userId) {
        if (!rule.allowSelf || userId == null) return false;

        var matcher = rule.pattern.matcher(path);
        return matcher.matches() && matcher.groupCount() > 0 && userId.equals(matcher.group(1));
    }

    // Helper to create rules quickly
    private static AccessRule rule(HttpMethod m, String p, String r, boolean s) {
        String regex = p.startsWith("^") ? p : "^" + Pattern.quote(p) + "$";
        return new AccessRule(m, Pattern.compile(regex), r, s);
    }

    private record AccessRule(HttpMethod method, Pattern pattern, String requiredPermission, boolean allowSelf) {
        boolean matches(String p, HttpMethod m) {
            return method == m && pattern.matcher(p).matches();
        }
    }
}
