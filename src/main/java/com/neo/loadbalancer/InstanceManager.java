package com.neo.loadbalancer;

import static java.util.Collections.synchronizedMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import com.neo.events.EventPublisher;
import com.neo.services.InstanceDefinitionService;

@Component
public class InstanceManager {

	private final Logger logger = LoggerFactory.getLogger(InstanceManager.class);

	private final Map<String, List<DefaultServiceInstance>> serviceInstances = synchronizedMap(
			new LinkedHashMap<String, List<DefaultServiceInstance>>());

	private final Environment environment;
	private final InstanceDefinitionService instanceDefinitionService;
	private final EventPublisher eventPublisher;

	@Autowired
	public InstanceManager(Environment environment, InstanceDefinitionService instanceDefinitionService,
			EventPublisher eventPublisher) {
		super();
		this.environment = environment;
		this.instanceDefinitionService = instanceDefinitionService;
		this.eventPublisher = eventPublisher;
	}

	public void refreshInstances(Map<String, Object> instances) {
		ConfigurableEnvironment configurableEnvironment = (ConfigurableEnvironment) environment;
		configurableEnvironment.getPropertySources().addFirst(new MapPropertySource("instance", instances));
	}

	@PostConstruct
	public void initInstances() {
		logger.info("Building instances....");
		serviceInstances.clear();

		serviceInstances.putAll(instanceDefinitionService.fetchInstanceDefinitions());
		logger.info("Building instances complete....");
		updateInstances();
	}

	private void updateInstances() {
		logger.info("Updating instances....");
		Map<String, Object> instances = new HashMap<>();
		serviceInstances.forEach((k, v) -> {
			for (int i = 0; i < v.size(); i++) {
				instances.put(getInstanceKey(v.get(i), i, "uri"), v.get(i).getUri());
				for (Entry<String, String> e : v.get(i).getMetadata().entrySet()) {
					instances.put(getInstanceKey(v.get(i), i, "metadata." + e.getKey()), e.getValue());
				}
			}
		});
		logger.info("Instances ready....");
		logger.info("Instances: {}", instances);
		refreshInstances(instances);
		eventPublisher.refreshInstances();
		logger.info("Instances updated successfully!!!!");
	}

	private String getInstanceKey(DefaultServiceInstance defaultServiceInstance, int index, String uriOrMetadata) {
		return "spring.cloud.discovery.client.simple.instances." + defaultServiceInstance.getServiceId() + "[" + index
				+ "]." + uriOrMetadata;
	}

}
