//package com.neo.crypt.impl;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//import java.security.SecureRandom;
//import java.security.SignatureException;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.Map;
//
//import org.bouncycastle.bcpg.ArmoredOutputStream;
//import org.bouncycastle.bcpg.CompressionAlgorithmTags;
//import org.bouncycastle.openpgp.PGPCompressedData;
//import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
//import org.bouncycastle.openpgp.PGPEncryptedData;
//import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
//import org.bouncycastle.openpgp.PGPEncryptedDataList;
//import org.bouncycastle.openpgp.PGPException;
//import org.bouncycastle.openpgp.PGPLiteralData;
//import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
//import org.bouncycastle.openpgp.PGPObjectFactory;
//import org.bouncycastle.openpgp.PGPOnePassSignature;
//import org.bouncycastle.openpgp.PGPOnePassSignatureList;
//import org.bouncycastle.openpgp.PGPPrivateKey;
//import org.bouncycastle.openpgp.PGPPublicKey;
//import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
//import org.bouncycastle.openpgp.PGPPublicKeyRing;
//import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
//import org.bouncycastle.openpgp.PGPSecretKey;
//import org.bouncycastle.openpgp.PGPSecretKeyRing;
//import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
//import org.bouncycastle.openpgp.PGPSignature;
//import org.bouncycastle.openpgp.PGPSignatureGenerator;
//import org.bouncycastle.openpgp.PGPSignatureList;
//import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
//import org.bouncycastle.openpgp.PGPUtil;
//import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
//import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
//import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
//import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
//import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
//import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
//import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
//import org.bouncycastle.util.io.Streams;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.neo.constant.CryptConstants;
//import com.neo.crypt.Crypt;
//
//import reactor.core.publisher.Mono;
//
//public class PgpCrypt extends Crypt {
//
//	private static final Logger logger = LoggerFactory.getLogger(PgpCrypt.class);
//	private static final String BC = "BC";
//
//	@Override
//	public Mono<String> encrypt(String plainText, Map<String, Object> cryptDetails) {
//		final boolean armored = (boolean) cryptDetails.get(CryptConstants.ARMORED);
//		final boolean withIntegrityPacket = (boolean) cryptDetails.get(CryptConstants.WITH_INTEGRITY_PACKET);
//		final int hashAlgorithmTags = (int) cryptDetails.get(CryptConstants.HASH_ALGORITHM_TAGS);
//		final int symmetricKeyAlgorithmTags = (int) cryptDetails.get(CryptConstants.SYMMETRIC_KEY_ALGORITHM_TAGS);
//		final String password = (String) cryptDetails.get(CryptConstants.PRIVATE_KEY_PASSWORD);
//		final PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
//		final PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(
//				CompressionAlgorithmTags.ZIP);
//		final PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(
//				new JcePGPDataEncryptorBuilder(symmetricKeyAlgorithmTags).setWithIntegrityPacket(withIntegrityPacket)
//						.setSecureRandom(new SecureRandom()).setProvider(BC));
//		final ByteArrayOutputStream out = new ByteArrayOutputStream();
//		final OutputStream theOut = armored ? new ArmoredOutputStream(out) : out;
//		try {
//
//			PGPPublicKey readPublicKey = readPublicKey(cryptDetails.get(CryptConstants.PUBLIC_KEY).toString());
//			encryptedDataGenerator.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(readPublicKey)
//					.setSecureRandom(new SecureRandom()).setProvider(BC));
//
//			final OutputStream encryptedOut = encryptedDataGenerator.open(theOut, new byte[4096]);
//
//			try (final OutputStream compressedOut = compressedDataGenerator.open(encryptedOut, new byte[4096]);) {
//
//				final PGPSecretKey pgpSec = readSecretKey(cryptDetails.get(CryptConstants.PRIVATE_KEY).toString());
//				final PGPPrivateKey privateKey = pgpSec.extractPrivateKey(
//						new JcePBESecretKeyDecryptorBuilder().setProvider(BC).build(password.toCharArray()));
//				final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
//						new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), hashAlgorithmTags)
//								.setProvider(BC));
//				signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
//				final Iterator<?> it = pgpSec.getPublicKey().getUserIDs();
//				if (it.hasNext()) {
//					final PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
//					spGen.addSignerUserID(false, (String) it.next());
//					signatureGenerator.setHashedSubpackets(spGen.generate());
//				}
//				signatureGenerator.generateOnePassVersion(false).encode(compressedOut);
//				try (final OutputStream literalOut = literalDataGenerator.open(compressedOut, PGPLiteralData.BINARY,
//						PGPLiteralData.CONSOLE, new Date(), new byte[4096]);) {
//					final InputStream in = new ByteArrayInputStream(plainText.getBytes(StandardCharsets.UTF_8));
//					final byte[] buf = new byte[4096];
//					for (int len; (len = in.read(buf)) > 0;) {
//						literalOut.write(buf, 0, len);
//						signatureGenerator.update(buf, 0, len);
//					}
//					in.close();
//					literalDataGenerator.close();
//					signatureGenerator.generate().encode(compressedOut);
//				}
//			}
//			return Mono.just(new String(out.toByteArray(), StandardCharsets.UTF_8));
//		} catch (IOException e) {
//			logger.error("IOException while PGPEncrypt. IOException : ", e);
//			return Mono.error(e);
//		} catch (PGPException e) {
//			logger.error("PGPException while PGPEncrypt. PGPException : ", e);
//			return Mono.error(e);
//		} finally {
//			try {
//				compressedDataGenerator.close();
//				encryptedDataGenerator.close();
//				theOut.close();
//			} catch (IOException e) {
//				logger.error("Exception occured while closing resources. IOException : ", e);
//			}
//		}
//	}
//
//	@Override
//	public Mono<String> decrypt(String ciphertext, Map<String, Object> cryptDetails) {
//		final String password = (String) cryptDetails.get(CryptConstants.PRIVATE_KEY_PASSWORD);
//		final boolean verifySignature = (boolean) cryptDetails.get(CryptConstants.VERIFY_SIGNATURE);
//
//		try {
//			Iterator<PGPEncryptedData> it = getEncryptedObjects(ciphertext.getBytes(StandardCharsets.UTF_8));
//
//			PGPPublicKeyEncryptedData pbe = (PGPPublicKeyEncryptedData) it.next();
//			final PGPSecretKey secretKey = readSecretKey(cryptDetails.get(CryptConstants.PRIVATE_KEY).toString());
//			PGPPrivateKey sKey = secretKey.extractPrivateKey(
//					new JcePBESecretKeyDecryptorBuilder().setProvider(BC).build(password.toCharArray()));
//			InputStream clear = pbe
//					.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(BC).build(sKey));
//
//			PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
//
//			PGPOnePassSignatureList onePassSignatureList = null;
//			PGPSignatureList signatureList = null;
//			PGPCompressedData compressedData;
//
//			Object message = plainFact.nextObject();
//			ByteArrayOutputStream actualOutput = new ByteArrayOutputStream();
//
//			while (message != null) {
//				if (message instanceof PGPCompressedData) {
//					compressedData = (PGPCompressedData) message;
//					plainFact = new PGPObjectFactory(compressedData.getDataStream(), new JcaKeyFingerprintCalculator());
//					message = plainFact.nextObject();
//				}
//
//				if (message instanceof PGPLiteralData) {
//					// have to read it and keep it somewhere.
//					Streams.pipeAll(((PGPLiteralData) message).getInputStream(), actualOutput);
//				} else if (message instanceof PGPOnePassSignatureList) {
//					onePassSignatureList = (PGPOnePassSignatureList) message;
//				} else if (message instanceof PGPSignatureList) {
//					signatureList = (PGPSignatureList) message;
//				} else {
//					throw new PGPException("message unknown message type.");
//				}
//				message = plainFact.nextObject();
//			}
//			actualOutput.close();
//			byte[] output = actualOutput.toByteArray();
//
//			// verify signature
//			if (verifySignature) {
//				verifySignature(onePassSignatureList, signatureList, cryptDetails, output);
//			}
//			if (pbe.isIntegrityProtected() && !pbe.verify()) {
//				throw new PGPException("Data is integrity protected but integrity is lost.");
//			}
//
//			return Mono.just(new String(actualOutput.toByteArray(), StandardCharsets.UTF_8));
//		} catch (IOException e) {
//			logger.error("IOException while PGPDecrypt. IOException : ", e);
//			return Mono.error(e);
//		} catch (PGPException e) {
//			logger.error("PGPException while PGPDecrypt. PGPException : ", e);
//			return Mono.error(e);
//		} catch (SignatureException e) {
//			logger.error("SignatureException while PGPDecrypt. SignatureException : ", e);
//			return Mono.error(e);
//		}
//	}
//
//	private void verifySignature(PGPOnePassSignatureList onePassSignatureList, PGPSignatureList signatureList,
//			Map<String, Object> cryptDetails, byte[] output) throws PGPException, SignatureException, IOException {
//		if (onePassSignatureList == null || signatureList == null) {
//			throw new PGPException("Poor PGP. Signatures not found.");
//		} else {
//			PGPPublicKey publicKey = readPublicKey(cryptDetails.get(CryptConstants.PUBLIC_KEY).toString());
//			for (int i = 0; i < onePassSignatureList.size(); i++) {
//				PGPOnePassSignature ops = onePassSignatureList.get(0);
//
//				ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider(BC), publicKey);
//				ops.update(output);
//				PGPSignature signature = signatureList.get(i);
//				if (!ops.verify(signature)) {
//					throw new SignatureException("Signature verification failed");
//				}
//			}
//		}
//	}
//
//	private  PGPPublicKey readPublicKey(String publicKey) throws IOException, PGPException {
//		try (InputStream is = new ByteArrayInputStream(publicKey.getBytes(StandardCharsets.UTF_8));) {
//			return readPublicKey(is);
//		}
//	}
//
//	private  PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
//		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input),
//				new JcaKeyFingerprintCalculator());
//
//		//
//		// we just loop through the collection till we find a key suitable for
//		// encryption, in the real
//		// world you would probably want to be a bit smarter about this.
//		//
//
//		Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
//		while (keyRingIter.hasNext()) {
//			PGPPublicKeyRing keyRing = keyRingIter.next();
//
//			Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
//			while (keyIter.hasNext()) {
//				PGPPublicKey key = keyIter.next();
//
//				if (key.isEncryptionKey()) {
//					return key;
//				}
//			}
//		}
//
//		throw new IllegalArgumentException("Can't find encryption key in key ring.");
//	}
//
//	private PGPSecretKey readSecretKey(String secretKey) throws IOException, PGPException {
//		try (InputStream is = new ByteArrayInputStream(secretKey.getBytes(StandardCharsets.UTF_8));) {
//			return readSecretKey(is);
//		}
//	}
//
//	private PGPSecretKey readSecretKey(InputStream input) throws IOException, PGPException {
//		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input),
//				new JcaKeyFingerprintCalculator());
//
//		//
//		// we just loop through the collection till we find a key suitable for
//		// encryption, in the real
//		// world you would probably want to be a bit smarter about this.
//		//
//
//		Iterator<PGPSecretKeyRing> keyRingIter = pgpSec.getKeyRings();
//		while (keyRingIter.hasNext()) {
//			PGPSecretKeyRing keyRing = keyRingIter.next();
//
//			Iterator<PGPSecretKey> keyIter = keyRing.getSecretKeys();
//			while (keyIter.hasNext()) {
//				PGPSecretKey key = keyIter.next();
//
//				if (key.isSigningKey()) {
//					return key;
//				}
//			}
//		}
//
//		throw new IllegalArgumentException("Can't find signing key in key ring.");
//	}
//
//	private  Iterator<PGPEncryptedData> getEncryptedObjects(final byte[] message) throws IOException {
//		try {
//			final PGPObjectFactory factory = new PGPObjectFactory(
//					PGPUtil.getDecoderStream(new ByteArrayInputStream(message)), new JcaKeyFingerprintCalculator());
//			final Object first = factory.nextObject();
//			final Object list = (first instanceof PGPEncryptedDataList) ? first : factory.nextObject();
//			return ((PGPEncryptedDataList) list).getEncryptedDataObjects();
//		} catch (IOException e) {
//			throw new IOException(e);
//		}
//	}
//
//}
