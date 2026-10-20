package com.digitilius.multireestr.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class LegacyOptionalStartupTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    LegacyDbInfo legacyDbInfo;

    @Test
    void jmixContextStartsWithoutLegacyDatabase() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("legacyJdbc")).isFalse();
        assertThat(legacyDbInfo.fetchPostgresVersion()).isEmpty();
    }
}
