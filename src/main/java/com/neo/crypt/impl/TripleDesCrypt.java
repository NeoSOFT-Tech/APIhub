//package com.neo.crypt.impl;
//
//import java.nio.charset.StandardCharsets;
//import java.security.GeneralSecurityException;
//import java.util.Map;
//
//import javax.crypto.Cipher;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
//import org.bouncycastle.util.encoders.Base64;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.neo.constant.CryptConstants;
//import com.neo.crypt.Crypt;
//
//import reactor.core.publisher.Mono;
//
//public class TripleDesCrypt extends Crypt {
//
//	private static final Logger logger = LoggerFactory.getLogger(TripleDesCrypt.class);
//	private static final String ALGORITHM = "TripleDES";
//
//	@Override
//	public Mono<String> encrypt(String plainText, Map<String, Object> cryptDetails) {
//		logger.debug("DES-CBC encryption started data : {}, cryptDetails : {}", plainText, cryptDetails);
//
//		try {
//			SecretKey key = getSecretKey(cryptDetails.get(CryptConstants.PASSPHRASE).toString());
//
//			logger.debug("SecretKey created. Now fetching cipher algorithm.");
//			Cipher cipher = Cipher.getInstance(ALGORITHM);
//
//			logger.debug("Now initializing cipher algorithm.");
//			cipher.init(Cipher.ENCRYPT_MODE, key);
//
//			logger.debug("Converting plaintext to byte array.");
//			byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
//
//			logger.debug("Encrypting byte array.");
//			byte[] buf = cipher.doFinal(plainTextBytes);
//
//			logger.debug("DES-CBC Encryption completed successfully.");
//			return Mono.just(Base64.toBase64String(buf));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while Encrypting DES-CBC : ", e);
//			return Mono.error(e);
//		}
//	}
//
//	@Override
//	public Mono<String> decrypt(String ciphertext, Map<String, Object> cryptDetails) {
//		logger.debug("DES-CBC decryption started data : {}, cryptDetails : {}", ciphertext, cryptDetails);
//		try {
//			SecretKey key = getSecretKey(cryptDetails.get(CryptConstants.PASSPHRASE).toString());
//
//			logger.debug("SecretKey created. Now fetching decipher algorithm.");
//			Cipher decipher = Cipher.getInstance(ALGORITHM);
//
//			logger.debug("Now initializing decipher algorithm.");
//			decipher.init(Cipher.DECRYPT_MODE, key);
//
//			logger.debug("Decoding ciphertext to byte array.");
//			byte[] message = Base64.decode(ciphertext.getBytes(StandardCharsets.UTF_8));
//
//			logger.debug("Decrypting byte array.");
//			byte[] plainText = decipher.doFinal(message);
//
//			logger.debug("DES-CBC Decryption completed successfully.");
//			return Mono.just(new String(plainText, StandardCharsets.UTF_8));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while decrypting DES-CBC: ", e);
//			return Mono.error(e);
//		}
//	}
//
//	private SecretKey getSecretKey(String passPhrase) {
//		logger.debug("Key size resolved. Now creating SecretKey.");
//		return new SecretKeySpec(Base64.decode(passPhrase), ALGORITHM);
//	}
//}