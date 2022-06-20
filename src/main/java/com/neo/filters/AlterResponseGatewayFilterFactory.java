package com.neo.filters;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class AlterResponseGatewayFilterFactory
		extends AbstractGatewayFilterFactory<AlterResponseGatewayFilterFactory.Config> {

	private final BodyMassager bodyMassager;
	private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

	@Autowired
	public AlterResponseGatewayFilterFactory(BodyMassager bodyMassager,
			ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory) {
		super(Config.class);
		this.bodyMassager = bodyMassager;
		this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return modifyResponseBodyGatewayFilterFactory.apply(c -> c.setRewriteFunction(String.class, String.class,
				(webExchange, responseBody) -> bodyMassager.massageResponse(webExchange, responseBody, config)));
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
