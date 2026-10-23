package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LegacyHikariFactoryTest {

    @Test
    void readOnlyFalseInConfigIsIgnored() {
        LegacyDataSourceProperties properties = new LegacyDataSourceProperties();
        properties.getHikari().setReadOnly(false);
        properties.getHikari().setMaximumPoolSize(3);

        HikariDataSource dataSource = new LegacyHikariFactory().create(
                "jdbc:postgresql://localhost:1/reestrdb",
                "reestr_ro",
                "secret",
                properties
        );
        try {
            assertThat(dataSource.isReadOnly()).isTrue();
            assertThat(dataSource.getDataSourceProperties().getProperty("readOnly")).isEqualTo("true");
            assertThat(dataSource.getDataSourceProperties().getProperty("readOnlyMode")).isEqualTo("always");
            assertThat(dataSource.getMaximumPoolSize()).isEqualTo(3);
        } finally {
            dataSource.close();
        }
    }
}
