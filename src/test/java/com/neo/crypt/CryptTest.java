package com.neo.crypt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import com.neo.constant.CryptConstants;
import com.neo.crypt.impl.AesCrypt;
import com.neo.crypt.impl.RsaCrypt;
import com.neo.crypt.impl.TripleDesCrypt;

import reactor.core.publisher.Mono;

@SpringBootTest
public class CryptTest {

	Crypt aesCrypt = new AesCrypt();
	Crypt tripleDesCrypt = new TripleDesCrypt();
	Crypt rsaCrypt = new RsaCrypt();
	static String textToCipher = "Hello, This string will be encrypted using AES-GCM algorithm : ";
	static String key = "ThisIsThe32ByteKeyForEncryption!";
	static String password = "Password@123";
	static Map<String, Object> aesCryptDetails = new HashMap<>();
	static Map<String, Object> desCryptDetails = new HashMap<>();
	static Map<String, Object> rsaCryptDetails = new HashMap<>();

	static {

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// encryption details for aes-gcm
		aesCryptDetails.put(CryptConstants.ASSOCIATED_DATA, "hello");
		aesCryptDetails.put(CryptConstants.ITERATION_COUNT, 1000);
		aesCryptDetails.put(CryptConstants.KEY_SIZE, 256);
		aesCryptDetails.put(CryptConstants.PASSPHRASE, key);
		aesCryptDetails.put(CryptConstants.PASSWORD, password);

		// encryption details for des-cbc
		desCryptDetails.put(CryptConstants.PASSPHRASE, "HIkyPuk72ZsWFbw4mzL+UhyJMj7pO9mb");

		// encryption details for aes-gcm
		rsaCryptDetails.put(CryptConstants.ASSOCIATED_DATA, "hello");
		rsaCryptDetails.put(CryptConstants.ITERATION_COUNT, 1000);
		rsaCryptDetails.put(CryptConstants.KEY_SIZE, 256);
		rsaCryptDetails.put(CryptConstants.PASSPHRASE, key);
		rsaCryptDetails.put(CryptConstants.PASSWORD, password);
		rsaCryptDetails.put(CryptConstants.PUBLIC_KEY,
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAptdvYtN1ooNlwulkoPr/jHiyTJChbzZo1tejtIg0wHA5p2Y717mDHs1cv/5HkU6PPcGk9cWQvtKDabIP6v/bXFIrxFCBdotuJU/3hMvcLcWBbgeP4++6zohQzMN8v5nBbE+8tBqoxD1JN1qVUAhXMgJvVWyOoILrKYUD9J0XqF+en8KYFr9ytzvk44nz7FwTwaq4hkCbK2PCCkOi5WuUIFX7o3qCXsYlxeqWzSTSiMR2M0BiBZLYCgQH1btrIIMUSaxI1YXfNqFyuRHDGqpgGkAVFrL7QEMX5niueESrSWkm+Wvc8gMVHAx6d3dY8TclGsvuaL59+e9YLsM6sNUrswIDAQAB");
		rsaCryptDetails.put(CryptConstants.PRIVATE_KEY,
				"MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCm129i03Wig2XC6WSg+v+MeLJMkKFvNmjW16O0iDTAcDmnZjvXuYMezVy//keRTo89waT1xZC+0oNpsg/q/9tcUivEUIF2i24lT/eEy9wtxYFuB4/j77rOiFDMw3y/mcFsT7y0GqjEPUk3WpVQCFcyAm9VbI6ggusphQP0nReoX56fwpgWv3K3O+TjifPsXBPBqriGQJsrY8IKQ6Lla5QgVfujeoJexiXF6pbNJNKIxHYzQGIFktgKBAfVu2sggxRJrEjVhd82oXK5EcMaqmAaQBUWsvtAQxfmeK54RKtJaSb5a9zyAxUcDHp3d1jxNyUay+5ovn3571guwzqw1SuzAgMBAAECggEAb9UuU2b8jd9XoavCCOnVy7nHgezcWOh2o7PNhqBaA7f13VB4wmQWENqE1ijCnhKzjyiyww8Xs+D1ty5k3xd7WUm3DpMMBIL3ZMlSV1AymPevhypId/fkFrZmuqo6f0+3yDs2eN75yvhtkE4GkavVjq71I6RtggffS2heu2NlTTUa9GeCqPCbXUQRf9bAdY6r23f5vBDyctGTMneYprGOzAt5lvEOJc8TZJwU5zPcj3KrJ01sTQfaj7g1bBaJ/436XFU/RDa4PgJSavXqo7dKHUAojR015JQPCM80cn2v/FRW1vHJCOrHDdFr9tGjMyXSPLIGrHAYYio/lXtY7OffwQKBgQDus6C+tfvKDumq0DpbIbajk2fT7DpDZskblBHdhsPnNO0MxcUtXsm+L8dUsoZP3J4knPccoOmLFhQ85FuShvN4lxZljpKVs6hhwWnfm5RfWSoSTUuDINGD+AXZ2slQZJdAJMBZ0ioh+5x14uf9E/FuVb/XoNjhx+gKu8wtXzPaEwKBgQCy7qodfPldM+IuZcIVdxPxvSAi1Y4vysf9H5v+2fKvbv7ErAHFnmtR5Ge/MQKYQwlDwiQpzwCQs3eq+4+trF3hxETIJCdmjVNDXlj6ypslk1r2yr7z8VkFzaBOuOrW/f5CoYv3tPz1YmQzaM2P+FRlsACVS4ajltAcZl2/ywOb4QKBgC6zaiGlzStNG55VuvUVdXwvXMLzlrqVI4eHwq8KrM1RZ4d2MgucXfAdvOvvOaPqT+5t31L0kHyrxy3ap6fVS1Euji00Za1wRuNJSgN7lt3IvhhykvGGvDcLkWl8CqL16zmbdx6bpa6cYZIW4Lh87GFf2XbAe25BBD8FWv8Nf0GNAoGAOjmrpbjbNFcDzPFT/MQo+0r9wBgaErc76MR2cLHfZPMLeFg4o2PvrFBU6kQ6ge2egCwWtHwlxPCBimQd1vf1/NdvQmlxYM+wPoXfEo0He2y99vz2sk7hFyBYgAVwddFlAa2r7Ek632bLEGeULLs0S3rAVdSj1WvoALXW3+hiHkECgYEAj4/olbTGxSSlfMw8K61CcnnBYoqNZ9fYV1UFt1pSl+Dr+g8i7HQSGkEqiahuacjBoJIDSbe/NTgcYeLJprkxFePrtvDEZkqgIJkKRPYBwGykf5xTbJjet1UpDeQ/cJAr757obebhkEb1rX6UaZQnYWOc/BHpdeh4aYPENoTeFD4=");
	}

//	@Test(invocationCount = 100, threadPoolSize = 5)
	public void aesCryptTest() throws Exception {
		Mono<String> cipherText = aesCrypt.encrypt(textToCipher + Thread.currentThread().getId(), aesCryptDetails);
		cipherText.subscribe(cipher -> {
			Mono<String> decipherText = aesCrypt.decrypt(cipher, aesCryptDetails);
			System.out.println("Encrypted Text : " + cipher);
			decipherText.subscribe(decipher -> {
				assertEquals(textToCipher + Thread.currentThread().getId(), decipher);
				System.out.println("Decrypted Text " + Thread.currentThread().getId() + " : " + decipher);
			});
		});
	}

//	@Test(invocationCount = 100, threadPoolSize = 5)
	public void tripleDesCryptTest() throws Exception {
		Mono<String> cipherText = tripleDesCrypt.encrypt(textToCipher + Thread.currentThread().getId(),
				desCryptDetails);
		cipherText.subscribe(cipher -> {
			Mono<String> decipherText = tripleDesCrypt.decrypt(cipher, desCryptDetails);
			System.out.println("DES-CBC Encrypted Text : " + cipher);
			decipherText.subscribe(decipher -> {
				assertEquals(textToCipher + Thread.currentThread().getId(), decipher);
				System.out.println("DES-CBC Decrypted Text " + Thread.currentThread().getId() + " : " + decipher);
			});
		});
	}

	@Test(invocationCount = 100, threadPoolSize = 5)
	public void rsaCryptTest() throws Exception {
		Mono<String> cipherText = rsaCrypt.encrypt(textToCipher + Thread.currentThread().getId(), rsaCryptDetails);
		cipherText.subscribe(cipher -> {
			Mono<String> decipherText = rsaCrypt.decrypt(cipher, rsaCryptDetails);
			System.out.println("AES-RSA Encrypted Text : " + cipher);
			decipherText.subscribe(decipher -> {
				assertEquals(textToCipher + Thread.currentThread().getId(), decipher);
				System.out.println("AES-RSA Decrypted Text " + Thread.currentThread().getId() + " : " + decipher);
			});
		});
	}

}