package com.neo.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

	private static final String EMPTY_KEY = "____EMPTY_KEY__";
	private static final String X_API_KEY = "x-api-key";

	@Bean
	public KeyResolver userKeyResolver() {
		// TODO: check api key with TPP mapping table
		return exchange -> {
			if (null == exchange.getRequest().getHeaders().getFirst(X_API_KEY)) {
				return Mono.just(EMPTY_KEY);
			}
			return Mono.just(exchange.getRequest().getHeaders().getFirst(X_API_KEY));
		};
	}
}
