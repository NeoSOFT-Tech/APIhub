package com.neo.filters;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.neo.exceptions.GatewayExceptionHandler;

@Component
public class AlterRequestGatewayFilterFactory
		extends AbstractGatewayFilterFactory<AlterRequestGatewayFilterFactory.Config> {

	private final BodyMassager bodyMassager;
	private final GatewayExceptionHandler gatewayExceptionHandler;

	@Autowired
	public AlterRequestGatewayFilterFactory(BodyMassager bodyMassager,
			GatewayExceptionHandler gatewayExceptionHandler) {
		super(Config.class);
		this.bodyMassager = bodyMassager;
		this.gatewayExceptionHandler = gatewayExceptionHandler;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ModifyRequestBodyGatewayFilterFactory.Config modifyRequestConfig = new ModifyRequestBodyGatewayFilterFactory.Config()
					.setRewriteFunction(String.class, String.class, (exchange1, originalRequestBody) -> bodyMassager
							.massageRequest(exchange, originalRequestBody, config));
			return new ModifyRequestBodyGatewayFilterFactory().apply(modifyRequestConfig).filter(exchange, chain)
					.onErrorMap(gatewayExceptionHandler::handleError);
		};
	}

	public static class Config {
		private boolean isProxy;
		private boolean isEncrypted;
		private boolean audit;
		private Map<String, String> encryptionDetails;

		public boolean isProxy() {
			return isProxy;
		}

		public void setProxy(boolean isProxy) {
			this.isProxy = isProxy;
		}

		public boolean isEncrypted() {
			return isEncrypted;
		}

		public void setEncrypted(boolean isEncrypted) {
			this.isEncrypted = isEncrypted;
		}

		public boolean isAudit() {
			return audit;
		}

		public void setAudit(boolean audit) {
			this.audit = audit;
		}

		public Map<String, String> getEncryptionDetails() {
			return encryptionDetails;
		}

		public void setEncryptionDetails(Map<String, String> encryptionDetails) {
			this.encryptionDetails = encryptionDetails;
		}
	}
}
