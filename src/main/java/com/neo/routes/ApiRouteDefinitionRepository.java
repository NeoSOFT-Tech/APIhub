package com.neo.routes;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ApiRouteDefinitionRepository implements RouteDefinitionRepository {

	private final Logger logger = LoggerFactory.getLogger(ApiRouteDefinitionRepository.class);
	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());
	private final RouteDefinitionService routeDefinitionService;

	@Autowired
	public ApiRouteDefinitionRepository(RouteDefinitionService routeDefinitionService) {
		super();
		this.routeDefinitionService = routeDefinitionService;
	}

	@PostConstruct
	public void initRoutes() {
		routes.clear();
		routeDefinitionService.fetchRouteDefinitions().forEach(routeDefinition -> {
			if (ObjectUtils.isEmpty(routeDefinition.getId())) {
				logger.error("id may not be empty");
			} else {
				routes.put(routeDefinition.getId(), routeDefinition);
			}
		});
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.flatMap(r -> {
			if (ObjectUtils.isEmpty(r.getId())) {
				return Mono.error(new IllegalArgumentException("id may not be empty"));
			}
			routes.put(r.getId(), r);
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