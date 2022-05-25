package com.neo.crypt;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neo.constant.CryptConstants;

import reactor.core.publisher.Mono;

public abstract class Crypt {

	private static final Logger logger = LoggerFactory.getLogger(Crypt.class);
	private static final String PBKDF_2_WITH_HMAC_SHA256 = "PBKDF2WithHmacSHA256";

	public abstract Mono<String> encrypt(String plainText, Map<String, Object> cryptDetails);

	public abstract Mono<String> decrypt(String ciphertext, Map<String, Object> cryptDetails);

	public Key getPasswordBasedKey(byte[] salt, Map<String, Object> cryptDetails, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		logger.trace("generating password based key with cryptDetails as : {}", cryptDetails);
		String password = cryptDetails.get(CryptConstants.PASSWORD).toString();
		int iterationCount = (int) cryptDetails.get(CryptConstants.ITERATION_COUNT);
		int keySize = (int) cryptDetails.get(CryptConstants.KEY_SIZE);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keySize);
		SecretKey pbeKey = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA256).generateSecret(pbeKeySpec);
		return new SecretKeySpec(pbeKey.getEncoded(), algorithm);
	}
}
