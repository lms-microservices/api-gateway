package com.lms.api_gateway.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RequestMutator {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    public ServerHttpRequest addUserHeaders(ServerHttpRequest request, String userId, String userEmail, String userRole) {
        return request.mutate()
                .header(HEADER_USER_ID, userId)
                .header(HEADER_USER_EMAIL, userEmail)
                .header(HEADER_USER_ROLE, userRole)
                .build();
    }
}
