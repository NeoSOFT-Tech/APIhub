package com.neo.routes;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.neo.services.RouteDefinitionService;
import com.neo.validator.RoutesValidator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ApiRouteDefinitionRepository implements RouteDefinitionRepository {

	private final Logger logger = LoggerFactory.getLogger(ApiRouteDefinitionRepository.class);
	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());
	private final RouteDefinitionService routeDefinitionService;
	private final RoutesValidator routesValidator;

	@Autowired
	public ApiRouteDefinitionRepository(RouteDefinitionService routeDefinitionService,
			RoutesValidator routesValidator) {
		super();
		this.routeDefinitionService = routeDefinitionService;
		this.routesValidator = routesValidator;
	}

	@PostConstruct
	public void initRoutes() {
		routes.clear();
		List<RouteDefinition> fetchRouteDefinitions = routeDefinitionService.fetchRouteDefinitions();
		fetchRouteDefinitions.forEach(routeDefinition -> {
			if (ObjectUtils.isEmpty(routeDefinition.getId())) {
				logger.error("id may not be empty");
			} else {
				if (routesValidator.validate(routeDefinition)) {
					routes.put(routeDefinition.getId(), routeDefinition);
				} else
					logger.warn("Invalid route definition by id : {}", routeDefinition.getId());
			}
		});
		logger.info("Total no of routes initialized successfully : {}", routes.size());
		logger.error("Total no of routes failed to initialize : {}", fetchRouteDefinitions.size() - routes.size());
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.flatMap(r -> {
			if (ObjectUtils.isEmpty(r.getId())) {
				return Mono.error(new IllegalArgumentException("id may not be empty"));
			}
			if (routesValidator.validate(r)) {
				routes.put(r.getId(), r);
			}
			return Mono.empty();
		});
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return routeId.flatMap(id -> {
			if (routes.containsKey(id)) {
				routes.remove(id);
				return Mono.empty();
			}
			return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
		});
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		Map<String, RouteDefinition> routesSafeCopy = new LinkedHashMap<>(routes);
		return Flux.fromIterable(routesSafeCopy.values());
	}

}