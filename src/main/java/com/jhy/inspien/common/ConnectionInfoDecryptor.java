
package com.jhy.inspien.common;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public final class ConnectionInfoDecryptor {

    private ConnectionInfoDecryptor() {}

    public static String decrypt(String encryptedText, String phoneNumber) throws Exception {

        // 1. 휴대폰 번호를 UTF-8 바이트로 변환
        byte[] phoneBytes = phoneNumber.getBytes(StandardCharsets.UTF_8);

        // 2. SHA-1 해시 생성
        byte[] hash = MessageDigest.getInstance("SHA-1").digest(phoneBytes);

        // 3. 해시 앞 16바이트를 AES 키로 사용
        byte[] keyBytes = Arrays.copyOf(hash, 16);
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // 4. Base64 디코딩
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedText.trim());

        // 5. AES-128 ECB PKCS5Padding 복호화
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
    }
}
