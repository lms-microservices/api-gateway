package com.lms.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Profile("!aws")
    public WebClient authWebClient(ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        return WebClient.builder()
                .filter(lbFunction)
                .baseUrl("http://auth-service")
                .build();
    }

    @Bean
    @Profile("aws")
    public WebClient authWebClientDirect(@Value("${auth.service.url:http://auth-service:8086}") String authServiceUrl) {
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
    }
}
