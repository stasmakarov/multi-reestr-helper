package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@ConditionalOnExpression("'${legacy.datasource.url:}'.length() > 0")
@EnableConfigurationProperties(LegacyDataSourceProperties.class)
public class LegacyDataSourceConfiguration {

    @Bean("legacy")
    DataSource legacyDataSource(LegacyDataSourceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        dataSource.setReadOnly(true);
        dataSource.addDataSourceProperty("readOnly", "true");
        dataSource.addDataSourceProperty("readOnlyMode", "always");

        LegacyDataSourceProperties.Hikari hikari = properties.getHikari();
        dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
        dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
        dataSource.setInitializationFailTimeout(hikari.getInitializationFailTimeout());
        return dataSource;
    }

    @Bean("legacyJdbc")
    NamedParameterJdbcTemplate legacyJdbc(@Qualifier("legacy") DataSource legacyDataSource) {
        return new NamedParameterJdbcTemplate(legacyDataSource);
    }
}
