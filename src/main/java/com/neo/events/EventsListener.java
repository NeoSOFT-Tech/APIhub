package com.neo.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.actuate.GatewayControllerEndpoint;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import com.neo.loadbalancer.InstanceManager;
import com.neo.routes.ApiRouteDefinitionRepository;

@Configuration
public class EventsListener {

	private final Logger logger = LoggerFactory.getLogger(EventsListener.class);

	private final InstanceManager instanceManager;
	private final ApiRouteDefinitionRepository apiRouteDefinitionRepository;

	@Autowired
	public EventsListener(InstanceManager instanceManager, ApiRouteDefinitionRepository apiRouteDefinitionRepository) {
		super();
		this.instanceManager = instanceManager;
		this.apiRouteDefinitionRepository = apiRouteDefinitionRepository;
	}

	@EventListener({ RefreshRoutesEvent.class })
	public void onRefresh(RefreshRoutesEvent event) {
		if (event.getSource() instanceof GatewayControllerEndpoint) {
			logger.info("Refreshing route-definition triggered by GatewayControllerEndpoint");
			apiRouteDefinitionRepository.initRoutes();
			logger.info("Refreshing instances triggered by GatewayControllerEndpoint");
			instanceManager.initInstances();
		}
	}
}