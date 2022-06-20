package com.neo.constant;

public class CryptConstants {

	private CryptConstants() {
		throw new IllegalStateException("CryptConstants class..");
	}

	public static final String PASSPHRASE = "passphrase";
	public static final String ITERATION_COUNT = "iterationCount";
	public static final String KEY_SIZE = "keySize";
	public static final String ASSOCIATED_DATA = "associatedData";
	public static final String PASSWORD = "password";
	public static final String PUBLIC_KEY = "publicKey";
	public static final String PRIVATE_KEY = "privateKey";
	public static final String ARMORED = "armored";
	public static final String HASH_ALGORITHM_TAGS = "hashAlgorithmTags";
	public static final String SYMMETRIC_KEY_ALGORITHM_TAGS = "symmetricKeyAlgorithmTags";
	public static final String WITH_INTEGRITY_PACKET = "withIntegrityPacket";
	public static final String VERIFY_SIGNATURE = "verifySignature";
	public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";
}
