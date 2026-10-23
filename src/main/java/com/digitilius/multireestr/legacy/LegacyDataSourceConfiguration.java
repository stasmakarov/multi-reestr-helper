package com.digitilius.multireestr.legacy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(LegacyDataSourceProperties.class)
public class LegacyDataSourceConfiguration {

    @Bean("legacy")
    SwappableLegacyDataSource legacyDataSource(
            LegacyDataSourceProperties properties,
            LegacyHikariFactory factory
    ) {
        SwappableLegacyDataSource wrapper = new SwappableLegacyDataSource();
        if (hasUrl(properties)) {
            wrapper.swap(factory.create(
                    properties.getUrl(),
                    properties.getUsername(),
                    properties.getPassword(),
                    properties
            ));
        }
        return wrapper;
    }

    @Bean("legacyJdbc")
    NamedParameterJdbcTemplate legacyJdbc(@Qualifier("legacy") DataSource legacyDataSource) {
        return new NamedParameterJdbcTemplate(legacyDataSource);
    }

    private static boolean hasUrl(LegacyDataSourceProperties properties) {
        return properties.getUrl() != null && !properties.getUrl().isBlank();
    }
}
