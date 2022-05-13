package com.neo.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class BodyMassager {

	private static final Logger logger = LoggerFactory.getLogger(BodyMassager.class);

	public Mono<String> massageRequest(ServerWebExchange webExchange, String originalBody,
			com.neo.filters.AlterRequestGatewayFilterFactory.Config config) {
		if (null != originalBody) {
			logger.info("Request URI : {}", webExchange.getRequest().getURI());
			logger.info("Request body : {}", originalBody);
			logger.info("Request headers : {}", webExchange.getRequest().getHeaders());
			return Mono.just(originalBody);
		} else {
			logger.info("Request headers : {}", webExchange.getRequest().getHeaders());
			return Mono.empty();
		}
	}

	public Mono<String> massageResponse(ServerWebExchange webExchange, String originalBody,
			com.neo.filters.AlterResponseGatewayFilterFactory.Config config) {
		if (null != originalBody) {
			logger.info("Response body : {}", originalBody);
			logger.info("Response headers : {}", webExchange.getRequest().getHeaders());
			return Mono.just(originalBody);
		} else {
			logger.info("Response headers : {}", webExchange.getRequest().getHeaders());
			return Mono.empty();
		}
	}
}