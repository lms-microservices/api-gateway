package com.lms.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AuthorizationRuleService {
    private final List<AccessRule> rules = List.of(
            rule(HttpMethod.GET, "/api/users", Set.of("ADMIN"), false),
            rule(HttpMethod.GET, "^/api/users/email/[^/]+$", Set.of("ADMIN"), false),
            rule(HttpMethod.PUT, "^/api/users/([^/]+)/approve-instructor$", Set.of("ADMIN"), false),
            rule(HttpMethod.GET, "^/api/users/([^/]+)$", Set.of("ADMIN"), true), // true = allowSelf
            rule(HttpMethod.PUT, "^/api/users/([^/]+)$", Set.of("ADMIN"), true)
    );

    public boolean isAuthorized(String path, HttpMethod method, String userId, String role) {
        return rules.stream()
                .filter(r -> r.matches(path, method))
                .findFirst()
                .map(r -> hasRole(r, role) || isSelf(r, path, userId))
                .orElse(true);
    }

    private boolean hasRole(AccessRule rule, String role) {
        return role != null && rule.allowedRoles.contains(role);
    }

    private boolean isSelf(AccessRule rule, String path, String userId) {
        if (!rule.allowSelf || userId == null) return false;

        var matcher = rule.pattern.matcher(path);
        return matcher.matches() && matcher.groupCount() > 0 && userId.equals(matcher.group(1));
    }

    // Helper to create rules quickly
    private static AccessRule rule(HttpMethod m, String p, Set<String> r, boolean s) {
        String regex = p.startsWith("^") ? p : "^" + Pattern.quote(p) + "$";
        return new AccessRule(m, Pattern.compile(regex), r, s);
    }

    private record AccessRule(HttpMethod method, Pattern pattern, Set<String> allowedRoles, boolean allowSelf) {
        boolean matches(String p, HttpMethod m) {
            return method == m && pattern.matcher(p).matches();
        }
    }
}
