package com.neo.filters;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.stereotype.Component;

import com.neo.audit.Auditor;

@Component
public class AlterResponseGatewayFilterFactory
		extends AbstractGatewayFilterFactory<AlterResponseGatewayFilterFactory.Config> {

	private final Auditor auditor;
	private final BodyMassager bodyMassager;
	private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

	@Autowired
	public AlterResponseGatewayFilterFactory(Auditor auditor, BodyMassager bodyMassager,
			ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory) {
		super(Config.class);
		this.auditor = auditor;
		this.bodyMassager = bodyMassager;
		this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return modifyResponseBodyGatewayFilterFactory.apply(c -> {
			c.setRewriteFunction(String.class, String.class, (webExchange, responseBody) -> {
				// TODO : Use below Config class attributes and make necessary changes
				auditor.auditResponse(webExchange, responseBody);
				return bodyMassager.massageResponse(webExchange, responseBody, config);
			});
		});
	}

	public static class Config {
		private boolean isProxy;
		private boolean isEncrypted;
		private boolean isAutoMsgId;
		private boolean audit;
		private String encryptionType;
		private Map<String, Object> encryptionDetails;

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

		public boolean isAutoMsgId() {
			return isAutoMsgId;
		}

		public void setAutoMsgId(boolean isAutoMsgId) {
			this.isAutoMsgId = isAutoMsgId;
		}

		public boolean isAudit() {
			return audit;
		}

		public void setAudit(boolean audit) {
			this.audit = audit;
		}

		public String getEncryptionType() {
			return encryptionType;
		}

		public void setEncryptionType(String encryptionType) {
			this.encryptionType = encryptionType;
		}

		public Map<String, Object> getEncryptionDetails() {
			return encryptionDetails;
		}

		public void setEncryptionDetails(Map<String, Object> encryptionDetails) {
			this.encryptionDetails = encryptionDetails;
		}
	}
}
