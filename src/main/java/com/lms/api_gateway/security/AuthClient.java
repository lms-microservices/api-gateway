package com.lms.api_gateway.security;

import com.lms.api_gateway.model.TokenValidationResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient {
    private final WebClient webClient;

    public AuthClient(WebClient authWebClient) {
        this.webClient = authWebClient;
    }

    public Mono<TokenValidationResponse> validate(String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/auth/validate")
                        .queryParam("token", token)
                        .build())
                .retrieve()
                .bodyToMono(TokenValidationResponse.class);
    }
}
