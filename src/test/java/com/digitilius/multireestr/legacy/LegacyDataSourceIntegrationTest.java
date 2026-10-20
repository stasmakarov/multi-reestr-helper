package com.digitilius.multireestr.legacy;

import com.digitilius.multireestr.legacy.test_support.LegacyPostgresTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class LegacyDataSourceIntegrationTest {

    @DynamicPropertySource
    static void legacyProperties(DynamicPropertyRegistry registry) {
        LegacyPostgresTestSupport.registerLegacyProperties(registry);
    }

    @Autowired
    LegacyDbInfo legacyDbInfo;

    @Autowired
    @Qualifier("legacyJdbc")
    NamedParameterJdbcTemplate legacyJdbc;

    @Test
    void postgresVersionContains96() {
        assertThat(legacyDbInfo.fetchPostgresVersion())
                .isPresent()
                .get()
                .asString()
                .contains("9.6");
    }

    @Test
    void insertThroughLegacyJdbcFailsAsReadOnly() {
        assertThatThrownBy(() -> legacyJdbc.update(
                "INSERT INTO legacy_probe(id) VALUES (2)",
                Collections.emptyMap()
        )).hasMessageMatching("(?s).*(read-only|read only|cannot execute).*");
    }

    @Test
    void createTableThroughLegacyJdbcFailsAsReadOnly() {
        assertThatThrownBy(() -> legacyJdbc.getJdbcTemplate()
                .execute("CREATE TABLE ro_fail(id int)"))
                .hasMessageMatching("(?s).*(read-only|read only|cannot execute|permission denied).*");
    }
}
