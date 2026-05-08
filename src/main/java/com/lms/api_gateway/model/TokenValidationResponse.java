package com.lms.api_gateway.model;

import java.util.List;

public record TokenValidationResponse(
        boolean valid,
        String userId,
        String email,
        String role,
        List<String> permissions
) {}
