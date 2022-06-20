package com.neo.filters;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.neo.audit.Auditor;
import com.neo.crypt.Crypt;
import com.neo.crypt.CryptFactory;
import com.neo.exceptions.CryptoException;

import reactor.core.publisher.Mono;

@Component
public class BodyMassager {

	private static final Logger logger = LoggerFactory.getLogger(BodyMassager.class);
	private final CryptFactory cryptFactory = new CryptFactory();

	private final Auditor auditor;

	@Autowired
	public BodyMassager(Auditor auditor) {
		this.auditor = auditor;
	}

	public Mono<String> massageRequest(ServerWebExchange webExchange, String originalBody,
			com.neo.filters.AlterRequestGatewayFilterFactory.Config config) {
		if (config.isAudit()) {
			auditor.auditRequest(webExchange, originalBody);
		}
		if (config.isProxy()) {
			return Mono.just(originalBody);
		}
		if (config.isEncrypted() && null != originalBody) {
			return handleDecryption(originalBody, config.getEncryptionDetails());
		}

		if (null != originalBody) {
			logger.debug("Request URI : {}", webExchange.getRequest().getURI());
			logger.debug("Request body : {}", originalBody);
			logger.debug("Request headers : {}", webExchange.getRequest().getHeaders());
			return Mono.just(originalBody);
		} else {
			logger.debug("Request headers : {}", webExchange.getRequest().getHeaders());
			return Mono.empty();
		}
	}

	private Mono<String> handleDecryption(String originalBody, Map<String, String> encryptionDetails) {
		try {
			Crypt crypt = cryptFactory.resolve(encryptionDetails);
			return Mono.just(crypt.decrypt(originalBody, encryptionDetails));
		} catch (CryptoException e) {
			logger.error("Exception occured while decrypting : ", e);
			return Mono.error(e);
		}
	}

	public Mono<String> massageResponse(ServerWebExchange webExchange, String originalBody,
			com.neo.filters.AlterResponseGatewayFilterFactory.Config config) {
		if (config.isAudit()) {
			auditor.auditResponse(webExchange, originalBody);
		}
		if (config.isProxy()) {
			return Mono.just(originalBody);
		}
		if (config.isEncrypted()) {
			Crypt crypt = cryptFactory.resolve(config.getEncryptionDetails());
			return (null != originalBody && !"".equals(originalBody))
					? handleEncryption(originalBody, config.getEncryptionDetails(), crypt)
					: Mono.empty();
		}

		if (null != originalBody) {
			logger.debug("Response body : {}", originalBody);
			logger.debug("Response headers : {}", webExchange.getRequest().getHeaders());
			return Mono.just(originalBody);
		} else {
			logger.debug("Response headers : {}", webExchange.getRequest().getHeaders());
			return Mono.empty();
		}
	}

	private Mono<String> handleEncryption(String originalBody, Map<String, String> encryptionDetails, Crypt crypt) {
		try {
			return Mono.just(crypt.encrypt(originalBody, encryptionDetails));
		} catch (CryptoException e) {
			logger.error("Exception occured while encrypting : ", e);
			return Mono.error(e);
		}
	}
}