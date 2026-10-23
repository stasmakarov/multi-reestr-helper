package com.digitilius.multireestr.legacy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class LegacySecretCipher {

    /**
     * Fixed salt for {@code Encryptors.delux}; not a secret. The secret is {@code MTR_SECRET_KEY}.
     * Hex of ASCII {@code mtrlegacy} (8 bytes).
     */
    static final String SALT_HEX = "6d74726c6567616379";

    private final TextEncryptor encryptor;

    public LegacySecretCipher(@Value("${mtr.secret-key:}") String secretKey) {
        this.encryptor = isBlank(secretKey) ? null : Encryptors.delux(secretKey, SALT_HEX);
    }

    public boolean isConfigured() {
        return encryptor != null;
    }

    public String encrypt(String plain) {
        requireConfigured();
        return encryptor.encrypt(plain == null ? "" : plain);
    }

    public String decrypt(String encrypted) {
        requireConfigured();
        if (encrypted == null || encrypted.isBlank()) {
            return "";
        }
        return encryptor.decrypt(encrypted);
    }

    private void requireConfigured() {
        if (encryptor == null) {
            throw new IllegalStateException("MTR_SECRET_KEY / mtr.secret-key is not set");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
