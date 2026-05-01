package com.lms.api_gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestMutator {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    public ServerHttpRequest addUserHeaders(
            ServerHttpRequest request,
            String userId,
            String userEmail,
            String userRole,
            List<String> permissions
    ) {
        return request.mutate()
                .header(HEADER_USER_ID, userId)
                .header(HEADER_USER_EMAIL, userEmail)
                .header(HEADER_USER_ROLE, userRole)
                .header(HEADER_USER_PERMISSIONS, String.join(",", permissions))
                .build();
    }
}
