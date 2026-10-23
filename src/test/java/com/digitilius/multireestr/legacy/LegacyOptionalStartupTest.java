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

    @Autowired
    SwappableLegacyDataSource legacyDataSource;

    @Test
    void jmixContextStartsWithoutLegacyDatabase() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("legacy")).isTrue();
        assertThat(applicationContext.containsBean("legacyJdbc")).isTrue();
        assertThat(legacyDataSource.hasPool()).isFalse();
        assertThat(legacyDbInfo.fetchPostgresVersion()).isEmpty();
        assertThat(legacyDbInfo.check().ok()).isFalse();
        assertThat(legacyDbInfo.check().error()).isEqualTo(LegacyDbStatus.NOT_CONFIGURED);
    }
}
