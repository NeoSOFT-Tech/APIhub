package com.neo.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class AuthenticationGatewayFilterFactory implements GatewayFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return null;
	}

}