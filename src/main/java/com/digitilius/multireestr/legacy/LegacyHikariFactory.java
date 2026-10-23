package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LegacyHikariFactory {

    private static final Logger log = LoggerFactory.getLogger(LegacyHikariFactory.class);

    public HikariDataSource create(
            String jdbcUrl,
            String username,
            String password,
            LegacyDataSourceProperties properties
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password == null ? "" : password);
        dataSource.setReadOnly(true);
        dataSource.addDataSourceProperty("readOnly", "true");
        dataSource.addDataSourceProperty("readOnlyMode", "always");

        LegacyDataSourceProperties.Hikari hikari = properties.getHikari();
        if (!hikari.isReadOnly()) {
            log.warn("legacy.datasource.hikari.read-only=false is ignored; legacy pool is always read-only");
        }
        dataSource.setMaximumPoolSize(hikari.getMaximumPoolSize());
        dataSource.setConnectionTimeout(hikari.getConnectionTimeout());
        dataSource.setInitializationFailTimeout(hikari.getInitializationFailTimeout());
        return dataSource;
    }

    public HikariDataSource create(
            String host,
            Integer port,
            String databaseName,
            String username,
            String password,
            LegacyDataSourceProperties properties
    ) {
        return create(LegacyJdbcUrls.build(host, port, databaseName), username, password, properties);
    }
}
