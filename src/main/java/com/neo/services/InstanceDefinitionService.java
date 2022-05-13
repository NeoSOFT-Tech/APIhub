package com.neo.services;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class InstanceDefinitionService {

	private final Logger logger = LoggerFactory.getLogger(InstanceDefinitionService.class);

	@Value("${instance.config.url}")
	private String routeConfigUrl;

	public Map<String, List<DefaultServiceInstance>> fetchInstanceDefinitions() {
		Map<String, List<DefaultServiceInstance>> defaultServiceInstance;
		logger.info("Fetching InstanceDefinition from URL: {}", routeConfigUrl);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Map<String, List<DefaultServiceInstance>>> response = restTemplate.exchange(routeConfigUrl,
				HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, List<DefaultServiceInstance>>>() {
				});
		defaultServiceInstance = response.getBody();
		return defaultServiceInstance;
	}
}
