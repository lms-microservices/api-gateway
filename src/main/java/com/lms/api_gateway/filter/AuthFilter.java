package com.lms.api_gateway.filter;

import com.lms.api_gateway.security.*;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<Object> {

    private final AuthClient authClient;
    private final PublicPathMatcher publicPathMatcher;
    private final AccessRuleEngine accessRuleEngine;
    private final JwtTokenExtractor jwtTokenExtractor;
    private final UserHeaderInjector userHeaderInjector;

    public AuthFilter(AuthClient authClient,
                      PublicPathMatcher publicPathMatcher,
                      AccessRuleEngine accessRuleEngine,
                      JwtTokenExtractor jwtTokenExtractor,
                      UserHeaderInjector userHeaderInjector) {
        this.authClient = authClient;
        this.publicPathMatcher = publicPathMatcher;
        this.accessRuleEngine = accessRuleEngine;
        this.jwtTokenExtractor = jwtTokenExtractor;
        this.userHeaderInjector = userHeaderInjector;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            HttpMethod method = request.getMethod();

            if (method == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            if (publicPathMatcher.isPublic(path, method)) {
                String token = jwtTokenExtractor.extract(request);
                if (token != null) {
                    return authClient.validate(token)
                            .map(response -> {
                                if (response.valid()) {
                                    return userHeaderInjector.addUserHeaders(request,
                                            response.userId(), response.email(), response.role(), response.permissions());
                                }
                                return request;
                            })
                            .onErrorResume(e -> Mono.just(request))
                            .flatMap(mutatedRequest -> chain.filter(exchange.mutate().request(mutatedRequest).build()));
                }
                return chain.filter(exchange);
            }

            String token = jwtTokenExtractor.extract(request);
            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            return authClient.validate(token)
                    .flatMap(response -> {
                        if (!response.valid()) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        String userId = response.userId();
                        String email = response.email();
                        String role = response.role();
                        var permissions = response.permissions();

                        if (!accessRuleEngine.isAuthorized(path, method, userId, permissions)) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }

                        ServerHttpRequest mutatedRequest = userHeaderInjector.addUserHeaders(
                                exchange.getRequest(), userId, email, role, permissions);

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    })
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
