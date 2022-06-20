//package com.neo.crypt.impl;
//
//import java.nio.ByteBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.GeneralSecurityException;
//import java.security.Key;
//import java.security.KeyFactory;
//import java.security.NoSuchAlgorithmException;
//import java.security.PrivateKey;
//import java.security.PublicKey;
//import java.security.SecureRandom;
//import java.security.spec.AlgorithmParameterSpec;
//import java.security.spec.InvalidKeySpecException;
//import java.security.spec.PKCS8EncodedKeySpec;
//import java.security.spec.X509EncodedKeySpec;
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
//
//import reactor.core.publisher.Mono;
//
//public class RsaCrypt extends AesCrypt {
//
//	private static final Logger logger = LoggerFactory.getLogger(RsaCrypt.class);
//	private static final String RSA = "RSA";
//	private static final String RSA_ALGORITHM = "RSA/None/OAEPWithSHA1AndMGF1Padding";
//	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
//	private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
//	private static final String AES = "AES";
//	private static final int IV_SIZE_IN_BYTES = 12;
//	private static final int SALT_SIZE_IN_BYTES = 16;
//	private static final int TAG_SIZE_IN_BYTES = 16;
//
//	@Override
//	public Mono<String> encrypt(String plainText, Map<String, Object> cryptDetails) {
//
//		logger.debug("RSA-AES-GCM encryption started data : {}, cryptDetails : {}", plainText, cryptDetails);
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
//			// --- wrap the new AES secret key ---
//			Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM, "BC");
//			rsaCipher.init(Cipher.WRAP_MODE, getPublicKey(cryptDetails.get(CryptConstants.PUBLIC_KEY).toString()));
//			logger.debug("RSA cipher initialization complete.");
//
//			byte[] wrappedKey = rsaCipher.wrap(key);
//			logger.debug("AES key wrapped and ready.");
//
//			AlgorithmParameterSpec algorithmParameterSpec = new GCMParameterSpec(TAG_SIZE_IN_BYTES * 8, iv);
//			final Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
//			aesCipher.init(Cipher.ENCRYPT_MODE, key, algorithmParameterSpec);
//			logger.debug("AES cipher initialization complete.");
//
//			if (null != cryptDetails.get(CryptConstants.ASSOCIATED_DATA)) {
//				aesCipher.updateAAD(
//						cryptDetails.get(CryptConstants.ASSOCIATED_DATA).toString().getBytes(StandardCharsets.UTF_8));
//				logger.debug("associated data updated successfully.");
//			}
//
//			byte[] cipherText = aesCipher.doFinal(plainTextByte);
//			logger.debug("plain text encrypted now attaching aesKeyRsaEnc and iv.");
//			int keySize = (int) cryptDetails.get(CryptConstants.KEY_SIZE);
//			ByteBuffer byteBuffer = ByteBuffer.allocate(
//					Short.BYTES + rsaCipher.getOutputSize(keySize / Byte.SIZE) + iv.length + cipherText.length);
//
//			// make sure that we know the size of the wrapped key
//			byteBuffer.putShort((short) wrappedKey.length);
//
//			// put in the wrapped key
//			byteBuffer.put(wrappedKey);
//
//			byteBuffer.put(iv);
//			byteBuffer.put(cipherText);
//			logger.debug("aesKeyRsaEnc-IV-CIPHERTEXT attached successfully.");
//
//			byte[] byteBufferArray = byteBuffer.array();
//			logger.debug("aesKeyRsaEnc-IV-CIPHERTEXT converted to byte array successfully.");
//
//			if (cipherText.length != plainTextByte.length + TAG_SIZE_IN_BYTES) {
//				// The tag is shorter than expected.
//				int actualTagSize = cipherText.length - plainTextByte.length;
//				logger.error("Encryption failed; RSA-AES-GCM tag must be {} bytes, but got only {} bytes.",
//						TAG_SIZE_IN_BYTES, actualTagSize);
//				throw new GeneralSecurityException(
//						String.format("encryption failed; RSA-AES-GCM tag must be %s bytes, but got only %s bytes",
//								TAG_SIZE_IN_BYTES, actualTagSize));
//			}
//			logger.debug("Successfully converted to bas64 string. RSA-AES-GCM encryption completed successfully.");
//			return Mono.just(Base64.toBase64String(byteBufferArray));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while Encrypting RSA-AES-GCM : ", e);
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
//			// get back the iv and salt from the cipher text
//			ByteBuffer bb = ByteBuffer.wrap(decode);
//			logger.debug("Converting Base64 decoded data to bytebuffer.");
//
//			// sanity check #1
//			if (bb.remaining() < 2) {
//				logger.error("Invalid cipher text less than 2.");
//				throw new IllegalArgumentException("Invalid ciphertext");
//			}
//			// the length of the encapsulated key
//			int wrappedKeySize = bb.getShort() & 0xFFFF;
//			// sanity check #2
//			if (bb.remaining() < wrappedKeySize + 128 / Byte.SIZE) {
//				logger.error("Invalid cipher text, less than wrapped key size.");
//				throw new IllegalArgumentException("Invalid ciphertext");
//			}
//
//			// get RSA wrapped AES key from cipher text
//			byte[] wrappedKey = new byte[wrappedKeySize];
//			bb.get(wrappedKey);
//			logger.debug("wrapped AES key extraction completed.");
//
//			// get iv from cipher text
//			byte[] iv = new byte[IV_SIZE_IN_BYTES];
//			bb.get(iv);
//			logger.debug("IV extraction completed.");
//
//			// get cipher text from cipher text as a whole
//			byte[] cipherTextByte = new byte[bb.remaining()];
//			bb.get(cipherTextByte);
//			logger.debug("CipherText extraction completed.");
//
//			final Cipher rsaCipher = Cipher.getInstance(RSA_ALGORITHM);
//			rsaCipher.init(Cipher.UNWRAP_MODE, getPrivateKey(cryptDetails.get(CryptConstants.PRIVATE_KEY).toString()));
//			logger.debug("RSA cipher initialization complete. Now decrypting key");
//
//			// UNWRAP AES key using private key
//			Key key = rsaCipher.unwrap(wrappedKey, AES, Cipher.SECRET_KEY);
//			logger.debug("RSA encrypted AES key, unwrapped successfully.");
//
//			final Cipher aesCipher = Cipher.getInstance(AES_ALGORITHM);
//			AlgorithmParameterSpec gcmIv = new GCMParameterSpec(TAG_SIZE_IN_BYTES * 8, iv);
//			aesCipher.init(Cipher.DECRYPT_MODE, key, gcmIv);
//			logger.debug("AES cipher initialization completed successfully. Now adding associated data if found.");
//
//			if (null != cryptDetails.get(CryptConstants.ASSOCIATED_DATA)) {
//				byte[] associatedDataByte = cryptDetails.get(CryptConstants.ASSOCIATED_DATA).toString()
//						.getBytes(StandardCharsets.UTF_8);
//				if (null != associatedDataByte && associatedDataByte.length != 0) {
//					aesCipher.updateAAD(associatedDataByte);
//					logger.debug("associated data found and added successfully.");
//				}
//			}
//
//			byte[] plainText = aesCipher.doFinal(cipherTextByte);
//			logger.debug("Successfully converted to byte array. RSA-AES-GCM decryption completed successfully.");
//			return Mono.just(new String(plainText, StandardCharsets.UTF_8));
//		} catch (GeneralSecurityException e) {
//			logger.error("Exception occured while decrypting RSA-AES-GCM : ", e);
//			return Mono.error(e);
//		}
//	}
//
//	private PublicKey getPublicKey(String base64PublicKey) {
//		PublicKey publicKey = null;
//		try {
//			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(base64PublicKey.getBytes()));
//			KeyFactory keyFactory = KeyFactory.getInstance(RSA);
//			publicKey = keyFactory.generatePublic(keySpec);
//			return publicKey;
//		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//			logger.error("Exception occured while resolving public key : {}, Exception : ", base64PublicKey, e);
//		}
//		return publicKey;
//	}
//
//	private PrivateKey getPrivateKey(String base64PrivateKey) {
//		PrivateKey privateKey = null;
//		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decode(base64PrivateKey.getBytes()));
//		KeyFactory keyFactory = null;
//		try {
//			keyFactory = KeyFactory.getInstance(RSA);
//			privateKey = keyFactory.generatePrivate(keySpec);
//		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//			logger.error("Exception occured while resolving server's private key : {}, Exception : {}",
//					base64PrivateKey, e);
//		}
//		return privateKey;
//	}
//
//}
