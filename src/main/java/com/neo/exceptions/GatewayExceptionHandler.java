package com.neo.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GatewayExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionHandler.class);

	public Throwable handleError(Throwable e) {
		if (e instanceof CryptoException) {
			CryptoException cryptoException = (CryptoException) e;
			logger.debug("Crypto error occured : ", e);
			return new ResponseStatusException(HttpStatus.valueOf(cryptoException.getCustomHttpStatusCode().getValue()),
					cryptoException.getCustomHttpStatusCode().getDescription());
		}
		// Custom exception can be added below...
		return e;
	}
}
