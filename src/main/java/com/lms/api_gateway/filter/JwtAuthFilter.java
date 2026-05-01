package com.lms.api_gateway.filter;

import com.lms.api_gateway.security.AuthorizationRuleService;
import com.lms.api_gateway.security.JwtTokenExtractor;
import com.lms.api_gateway.security.JwtValidator;
import com.lms.api_gateway.security.PublicPathChecker;
import com.lms.api_gateway.security.RequestMutator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtTokenExtractor tokenExtractor;
    private final JwtValidator jwtValidator;
    private final PublicPathChecker publicPathChecker;
    private final AuthorizationRuleService authorizationRuleService;
    private final RequestMutator requestMutator;

    public JwtAuthFilter(JwtTokenExtractor tokenExtractor,
                         JwtValidator jwtValidator,
                         PublicPathChecker publicPathChecker,
                         AuthorizationRuleService authorizationRuleService,
                         RequestMutator requestMutator) {
        super(Config.class);
        this.tokenExtractor = tokenExtractor;
        this.jwtValidator = jwtValidator;
        this.publicPathChecker = publicPathChecker;
        this.authorizationRuleService = authorizationRuleService;
        this.requestMutator = requestMutator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            var method = request.getMethod();

            if (publicPathChecker.isPublic(path, method)) {
                return chain.filter(exchange);
            }

            String token = tokenExtractor.extract(request);
            if (token == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                Claims claims = jwtValidator.validateAndParse(token);
                String userId = jwtValidator.getClaim(claims, "userId");
                String userEmail = jwtValidator.getClaim(claims, "email");
                String userRole = jwtValidator.getClaim(claims, "role");
                List<String> permissions = jwtValidator.getStringListClaim(claims, "permissions");

                if (!authorizationRuleService.isAuthorized(path, method, userId, permissions)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                ServerHttpRequest mutatedRequest = requestMutator.addUserHeaders(request, userId, userEmail, userRole, permissions);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (ExpiredJwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } catch (JwtException e) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
    }
}
