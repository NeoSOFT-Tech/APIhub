package com.neo.audit;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.neo.constant.Constants;

@Component
public class Auditor {

	private static final Logger logger = LoggerFactory.getLogger(Auditor.class);

	public void auditRequest(ServerWebExchange exchange, String body) {
		logger.info("Request status : 02");
		logger.info("Request body : {}", body);
		logger.info("Request headers : {}", exchange.getRequest().getHeaders());
		logger.info("Msgid header : {}",
				exchange.getRequest().getHeaders().getOrDefault(Constants.MSG_ID, Arrays.asList("NO MSGID")));
		logger.info("MsgRrn header : {}",
				exchange.getRequest().getHeaders().getOrDefault(Constants.MSG_RRN, Arrays.asList("NO MSGRRN")));
	}

	public void auditResponse(ServerWebExchange exchange, String body) {
		logger.info("Response body : {}", body);
		logger.info("Response headers : {}", exchange.getResponse().getHeaders());
		logger.info("Response http status : {}", exchange.getResponse().getRawStatusCode());
	}
}
