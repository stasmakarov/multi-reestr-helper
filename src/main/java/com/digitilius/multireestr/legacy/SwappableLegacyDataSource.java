package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicReference;

public class SwappableLegacyDataSource extends DelegatingDataSource implements AutoCloseable {

    private final AtomicReference<HikariDataSource> current = new AtomicReference<>();

    @Override
    public void afterPropertiesSet() {
        // Target may be absent until properties or UI settings supply a pool.
    }

    @Override
    protected DataSource obtainTargetDataSource() {
        HikariDataSource dataSource = current.get();
        if (dataSource == null) {
            throw new LegacyNotConfiguredException();
        }
        return dataSource;
    }

    public synchronized void swap(HikariDataSource next) {
        HikariDataSource previous = current.getAndSet(next);
        if (next != null) {
            setTargetDataSource(next);
        } else {
            setTargetDataSource(null);
        }
        if (previous != null && previous != next) {
            previous.close();
        }
    }

    public boolean hasPool() {
        return current.get() != null;
    }

    public String getCurrentJdbcUrl() {
        HikariDataSource dataSource = current.get();
        return dataSource == null ? null : dataSource.getJdbcUrl();
    }

    @Override
    public void close() {
        swap(null);
    }
}
