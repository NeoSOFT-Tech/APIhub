package com.neo.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RouteDefinitionService {

	private final Logger logger = LoggerFactory.getLogger(RouteDefinitionService.class);

	@Value("${route.config.url}")
	private String routeConfigUrl;

	public List<RouteDefinition> fetchRouteDefinitions() {
		List<RouteDefinition> gatewayDefinitions;
		logger.info("Fetching GatewayDefinition from URL: {}", routeConfigUrl);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<List<RouteDefinition>> response = restTemplate.exchange(routeConfigUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<RouteDefinition>>() {
				});
		gatewayDefinitions = response.getBody();
		return gatewayDefinitions;
	}
}
