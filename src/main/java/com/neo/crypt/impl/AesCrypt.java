//package com.neo.crypt.impl;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.GeneralSecurityException;
//import java.security.Key;
//import java.security.SecureRandom;
//import java.security.spec.AlgorithmParameterSpec;
//import java.util.Map;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.GCMParameterSpec;
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
//public class AesCrypt extends Crypt {
//
//	private static final Logger logger = LoggerFactory.getLogger(AesCrypt.class);
//	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//	private static final String ALGORITHM = "AES/GCM/NoPadding";
//	private static final String AES = "AES";
//	private static final int IV_SIZE_IN_BYTES = 12;
//	private static final int SALT_SIZE_IN_BYTES = 16;
//	private static final int TAG_SIZE_IN_BYTES = 16;
//
//	@Override
//	public Mono<String> encrypt(final String plainText, final Map<String, Object> cryptDetails) {
//		logger.debug("AES-GCM encryption started data : {}, cryptDetails : {}", plainText, cryptDetails);
//		try {
//			final byte[] plainTextByte = plainText.getBytes(StandardCharsets.UTF_8);
//			logger.debug("data converted to byte[] successfully");
//			// Check that ciphertext is not longer than the max. size of a Java array.
//			if (plainTextByte.length > Integer.MAX_VALUE - IV_SIZE_IN_BYTES - TAG_SIZE_IN_BYTES) {
//				logger.error("plaint text size is more than {}",
//						Integer.MAX_VALUE - IV_SIZE_IN_BYTES - TAG_SIZE_IN_BYTES);
//				throw new GeneralSecurityException("plaintext too long");
//			}
//
//			// generate 12 bytes iv
//			byte[] iv = new byte[IV_SIZE_IN_BYTES];
//			SECURE_RANDOM.nextBytes(iv);
//			logger.debug("IV generation complete.");
//
//			// generate 16 bytes salt
//			byte[] salt = new byte[SALT_SIZE_IN_BYTES];
//			SECURE_RANDOM.nextBytes(salt);
//			logger.debug("SALT generation complete.");
//
//			// generate password based secret key (encryption / decryption key)
//			Key key = getPasswordBasedKey(salt, cryptDetails, AES);
//			logger.debug("password based key generation complete.");
//
//			AlgorithmParameterSpec algorithmParameterSpec = new GCMParameterSpec(TAG_SIZE_IN_BYTES * 8, iv);
//			final Cipher cipher = Cipher.getInstance(ALGORITHM);
//			cipher.init(Cipher.ENCRYPT_MODE, key, algorithmParameterSpec);
//			logger.debug("cipher initialization complete.");
//
//			if (null != cryptDetails.get(CryptConstants.ASSOCIATED_DATA)) {
//				cipher.updateAAD(
//						cryptDetails.get(CryptConstants.ASSOCIATED_DATA).toString().getBytes(StandardCharsets.UTF_8));
//				logger.debug("associated data updated successfully.");
//			}
//
//			byte[] cipherText = cipher.doFinal(plainTextByte);
//			logger.debug("plain text encrypted now attaching iv an salt.");
//			ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + salt.length + cipherText.length);
//			byteBuffer.put(iv);
//			byteBuffer.put(salt);
//			byteBuffer.put(cipherText);
//			logger.debug("IV-SALT-CIPHERTEXT attached successfully.");
//
//			byte[] byteBufferArray = byteBuffer.array();
//			logger.debug("IV-SALT-CIPHERTEXT converted to byte array successfully.");
//
//			if (cipherText.length != plainTextByte.length + TAG_SIZE_IN_BYTES) {
//				// The tag is shorter than expected.
//				int actualTagSize = cipherText.length - plainTextByte.length;
//				logger.error("Encryption failed; GCM tag must be {} bytes, but got only {} bytes.", TAG_SIZE_IN_BYTES,
//						actualTagSize);
//				throw new GeneralSecurityException(
//						String.format("encryption failed; GCM tag must be %s bytes, but got only %s bytes",
//								TAG_SIZE_IN_BYTES, actualTagSize));
//			}
//			logger.debug("Successfully converted to bas64 string. AES-GCM encryption completed successfully.");
//			return Mono.just(Base64.toBase64String(byteBufferArray));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while Encrypting AES-GCM : ", e);
//			return Mono.error(e);
//		}
//	}
//
//	@Override
//	public Mono<String> decrypt(String cipherText, Map<String, Object> cryptDetails) {
//		try {
//			byte[] decode = Base64.decode(cipherText.getBytes(StandardCharsets.UTF_8));
//			logger.debug("Base64 decoded and data converted to byte[] successfully.");
//
//			// check encrypted text length
//			if (decode.length < IV_SIZE_IN_BYTES + SALT_SIZE_IN_BYTES + TAG_SIZE_IN_BYTES) {
//				logger.error("Encrypted data is too short for decryption.");
//				throw new GeneralSecurityException("ciphertext too short");
//			}
//
//			// get back the iv and salt from the cipher text
//			ByteBuffer bb = ByteBuffer.wrap(decode);
//			logger.debug("Converting Base64 decoded data to bytebuffer.");
//
//			// get iv from cipher text
//			byte[] iv = new byte[IV_SIZE_IN_BYTES];
//			bb.get(iv);
//			logger.debug("IV extraction completed.");
//
//			// get salt from cipher text
//			byte[] salt = new byte[SALT_SIZE_IN_BYTES];
//			bb.get(salt);
//			logger.debug("SALT extraction completed.");
//
//			// get cipher text from cipher text as a whole
//			byte[] cipherTextByte = new byte[bb.remaining()];
//			bb.get(cipherTextByte);
//			logger.debug("CipherText extraction completed. Now resolving key");
//
//			// generate password based secret key (encryption / decryption key)
//			Key key = getPasswordBasedKey(salt, cryptDetails, AES);
//			logger.debug("Password based key resolved successfully.");
//
//			final Cipher cipher = Cipher.getInstance(ALGORITHM);
//			AlgorithmParameterSpec gcmIv = new GCMParameterSpec(TAG_SIZE_IN_BYTES * 8, iv);
//			cipher.init(Cipher.DECRYPT_MODE, key, gcmIv);
//			logger.debug("Cipher initialization completed successfully. Now adding associated data if found.");
//
//			if (null != cryptDetails.get(CryptConstants.ASSOCIATED_DATA)) {
//				byte[] associatedDataByte = cryptDetails.get(CryptConstants.ASSOCIATED_DATA).toString()
//						.getBytes(StandardCharsets.UTF_8);
//				if (null != associatedDataByte && associatedDataByte.length != 0) {
//					cipher.updateAAD(associatedDataByte);
//					logger.debug("associated data found and added successfully.");
//				}
//			}
//
//			byte[] plainText = cipher.doFinal(cipherTextByte);
//			logger.debug("Successfully converted to byte array. AES-GCM decryption completed successfully.");
//			return Mono.just(new String(plainText, StandardCharsets.UTF_8));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while decrypting AES-GCM : ", e);
//			return Mono.error(e);
//		}
//	}
//}