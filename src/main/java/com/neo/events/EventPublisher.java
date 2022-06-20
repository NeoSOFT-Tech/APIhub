package com.neo.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventPublisher implements ApplicationEventPublisherAware {

	private final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void refreshRoutes() {
		logger.info("Now forcing new routes.....");
		applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
		logger.info("New routes loaded successfully.....");
	}

	public void refreshInstances() {
		logger.info("Now forcing new Instances.....");
		applicationEventPublisher.publishEvent(new RefreshEvent(this, "RefreshEvent", "Refreshing scope"));
		logger.info("New instances loaded successfully.....");
	}
}
