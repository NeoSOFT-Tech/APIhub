package com.neo.validator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Component;

import com.neo.constant.Constants;

/**
 * @author user
 *
 *         Validate routes definition provided by MW
 */
@Component
public class RoutesValidator {

	private final Logger logger = LoggerFactory.getLogger(RoutesValidator.class);
	private ValidatorFactory validatorFactory = new ValidatorFactory();

	public boolean validate(RouteDefinition routeDefinition) {
		try {
			routeDefinition.getFilters().forEach(filterDefinition -> {
				filterDefinition.getName();
				filterDefinition.getArgs();
				if (filterDefinition.getArgs().containsKey(Constants.ENCRYPTED)) {
					boolean encrypted = Boolean.parseBoolean(filterDefinition.getArgs().get(Constants.ENCRYPTED));
					if (encrypted) {
						validatorFactory.validate(resolveFilterArgs(filterDefinition.getArgs()));
					}
				}
			});
			return true;
		} catch (IllegalArgumentException e) {
			logger.error("Route by id : {} is incorrectly configured, reason : {}", routeDefinition.getId(), e);
			return false;
		}
	}

	private Map<String, String> resolveFilterArgs(Map<String, String> args) {
		return args.entrySet().stream().filter(k -> k.getKey().contains("."))
				.collect(Collectors.toMap(e -> e.getKey().split("\\.")[1], Entry::getValue));
	}
}
