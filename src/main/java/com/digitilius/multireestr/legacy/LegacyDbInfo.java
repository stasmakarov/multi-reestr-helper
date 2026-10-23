package com.digitilius.multireestr.legacy;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Optional;

@Component
public class LegacyDbInfo {

    private final NamedParameterJdbcTemplate legacyJdbc;
    private final DataSource legacyDataSource;

    @Autowired
    public LegacyDbInfo(
            @Qualifier("legacyJdbc") NamedParameterJdbcTemplate legacyJdbc,
            @Qualifier("legacy") DataSource legacyDataSource
    ) {
        this.legacyJdbc = legacyJdbc;
        this.legacyDataSource = legacyDataSource;
    }

    public Optional<String> fetchPostgresVersion() {
        if (!hasPool()) {
            return Optional.empty();
        }
        try {
            String version = legacyJdbc.queryForObject(
                    "select version()",
                    Collections.emptyMap(),
                    String.class
            );
            return Optional.ofNullable(version);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public LegacyDbStatus check() {
        return check(legacyJdbc, currentEndpoint());
    }

    public LegacyDbStatus check(NamedParameterJdbcTemplate jdbc, LegacyJdbcUrls.JdbcEndpoint endpoint) {
        if (jdbc == null) {
            return notConfigured(endpoint);
        }
        long started = System.nanoTime();
        try {
            return jdbc.getJdbcTemplate().query(
                    """
                            select version() as server_version,
                                   current_user as db_user,
                                   current_database() as db_name,
                                   current_setting('transaction_read_only') as tx_read_only,
                                   coalesce((select r.rolsuper from pg_roles r where r.rolname = current_user), false) as is_super,
                                   has_schema_privilege(current_user, 'public', 'CREATE') as can_create
                            """,
                    rs -> {
                        long responseMs = elapsedMs(started);
                        if (!rs.next()) {
                            return fail(endpoint, responseMs, "Empty result from version query");
                        }
                        String database = rs.getString("db_name");
                        boolean privilegeWarning = isOn(rs.getString("is_super"))
                                || isOn(rs.getString("can_create"));
                        return new LegacyDbStatus(
                                true,
                                rs.getString("server_version"),
                                rs.getString("db_user"),
                                database != null ? database : endpoint.database(),
                                endpoint.host(),
                                endpoint.port(),
                                isOn(rs.getString("tx_read_only")),
                                responseMs,
                                null,
                                privilegeWarning
                        );
                    }
            );
        } catch (LegacyNotConfiguredException e) {
            return notConfigured(endpoint);
        } catch (Exception e) {
            return fail(endpoint, elapsedMs(started), safeMessage(e));
        }
    }

    public boolean hasPool() {
        return legacyDataSource instanceof SwappableLegacyDataSource swappable && swappable.hasPool();
    }

    public LegacyJdbcUrls.JdbcEndpoint currentEndpoint() {
        if (legacyDataSource instanceof SwappableLegacyDataSource swappable) {
            return LegacyJdbcUrls.parse(swappable.getCurrentJdbcUrl());
        }
        if (legacyDataSource instanceof HikariDataSource hikari) {
            return LegacyJdbcUrls.parse(hikari.getJdbcUrl());
        }
        return LegacyJdbcUrls.parse(null);
    }

    private static LegacyDbStatus notConfigured(LegacyJdbcUrls.JdbcEndpoint endpoint) {
        return new LegacyDbStatus(
                false,
                null,
                null,
                endpoint.database(),
                endpoint.host(),
                endpoint.port(),
                false,
                0L,
                LegacyDbStatus.NOT_CONFIGURED,
                false
        );
    }

    private static LegacyDbStatus fail(LegacyJdbcUrls.JdbcEndpoint endpoint, long responseMs, String error) {
        return new LegacyDbStatus(
                false,
                null,
                null,
                endpoint.database(),
                endpoint.host(),
                endpoint.port(),
                false,
                responseMs,
                error,
                false
        );
    }

    private static long elapsedMs(long startedNanos) {
        return Math.max(0L, (System.nanoTime() - startedNanos) / 1_000_000L);
    }

    private static boolean isOn(String value) {
        return "on".equalsIgnoreCase(value)
                || "true".equalsIgnoreCase(value)
                || "t".equalsIgnoreCase(value);
    }

    static String safeMessage(Throwable throwable) {
        String message = throwable.getMessage();
        if (message == null || message.isBlank()) {
            return throwable.getClass().getSimpleName();
        }
        return message.replaceAll("(?i)password=[^;&\\s]*", "password=***");
    }
}
