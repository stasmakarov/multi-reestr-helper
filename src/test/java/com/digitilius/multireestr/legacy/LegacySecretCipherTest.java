package com.digitilius.multireestr.legacy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacySecretCipherTest {

    @Test
    void encryptAndDecryptRoundTrip() {
        LegacySecretCipher cipher = new LegacySecretCipher("test-secret-key");
        String encrypted = cipher.encrypt("reestr_ro");
        assertThat(encrypted).isNotBlank().isNotEqualTo("reestr_ro");
        assertThat(cipher.decrypt(encrypted)).isEqualTo("reestr_ro");
    }

    @Test
    void missingKeyRefusesEncrypt() {
        LegacySecretCipher cipher = new LegacySecretCipher("");
        assertThat(cipher.isConfigured()).isFalse();
        assertThatThrownBy(() -> cipher.encrypt("secret"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MTR_SECRET_KEY");
    }
}
