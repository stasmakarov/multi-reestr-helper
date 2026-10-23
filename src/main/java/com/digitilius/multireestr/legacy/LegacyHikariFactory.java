package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

@Component
public class LegacyHikariFactory {

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
        dataSource.setReadOnly(hikari.isReadOnly());
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
