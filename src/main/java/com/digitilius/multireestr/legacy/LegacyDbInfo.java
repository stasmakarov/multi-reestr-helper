package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

@Component
public class LegacyDbInfo {

    private static final String JDBC_PREFIX = "jdbc:";

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final DataSource legacyDataSource;

    @Autowired
    public LegacyDbInfo(
            @Autowired(required = false) @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            @Autowired(required = false) @Qualifier("legacy") DataSource legacyDataSource
    ) {
        this.legacyJdbc = legacyJdbc;
        this.legacyDataSource = legacyDataSource;
    }

    public Optional<String> fetchPostgresVersion() {
        if (legacyJdbc == null) {
            return Optional.empty();
        }
        String version = legacyJdbc.queryForObject(
                "select version()",
                Collections.emptyMap(),
                String.class
        );
        return Optional.ofNullable(version);
    }

    public LegacyDbStatus check() {
        JdbcEndpoint endpoint = parseEndpoint(legacyDataSource);
        if (legacyJdbc == null) {
            return new LegacyDbStatus(
                    false,
                    null,
                    null,
                    endpoint.database(),
                    endpoint.host(),
                    endpoint.port(),
                    false,
                    0L,
                    "Legacy DataSource is not configured"
            );
        }
        long started = System.nanoTime();
        try {
            return legacyJdbc.getJdbcTemplate().query(
                    """
                            select version() as server_version,
                                   current_user as db_user,
                                   current_database() as db_name,
                                   current_setting('transaction_read_only') as tx_read_only
                            """,
                    rs -> {
                        long responseMs = elapsedMs(started);
                        if (!rs.next()) {
                            return new LegacyDbStatus(
                                    false,
                                    null,
                                    null,
                                    endpoint.database(),
                                    endpoint.host(),
                                    endpoint.port(),
                                    false,
                                    responseMs,
                                    "Empty result from version query"
                            );
                        }
                        String database = rs.getString("db_name");
                        return new LegacyDbStatus(
                                true,
                                rs.getString("server_version"),
                                rs.getString("db_user"),
                                database != null ? database : endpoint.database(),
                                endpoint.host(),
                                endpoint.port(),
                                isOn(rs.getString("tx_read_only")),
                                responseMs,
                                null
                        );
                    }
            );
        } catch (Exception e) {
            return new LegacyDbStatus(
                    false,
                    null,
                    null,
                    endpoint.database(),
                    endpoint.host(),
                    endpoint.port(),
                    false,
                    elapsedMs(started),
                    safeMessage(e)
            );
        }
    }

    private static long elapsedMs(long startedNanos) {
        return Math.max(0L, (System.nanoTime() - startedNanos) / 1_000_000L);
    }

    private static boolean isOn(String value) {
        return "on".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    private static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message.replaceAll("(?i)password=[^;&\\s]*", "password=***");
    }

    static JdbcEndpoint parseEndpoint(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource hikari) {
            return parseJdbcUrl(hikari.getJdbcUrl());
        }
        return JdbcEndpoint.empty();
    }

    static JdbcEndpoint parseJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return JdbcEndpoint.empty();
        }
        try {
            String withoutJdbc = jdbcUrl.startsWith(JDBC_PREFIX)
                    ? jdbcUrl.substring(JDBC_PREFIX.length())
                    : jdbcUrl;
            URI uri = URI.create(withoutJdbc);
            String host = uri.getHost();
            Integer port = uri.getPort() >= 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            String database = null;
            if (path != null && path.length() > 1) {
                database = path.substring(1);
                int slash = database.indexOf('/');
                if (slash >= 0) {
                    database = database.substring(0, slash);
                }
            }
            return new JdbcEndpoint(host, port, database);
        } catch (Exception e) {
            return JdbcEndpoint.empty();
        }
    }

    record JdbcEndpoint(String host, Integer port, String database) {
        static JdbcEndpoint empty() {
            return new JdbcEndpoint(null, null, null);
        }
    }
}
