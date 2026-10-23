package com.digitilius.multireestr.legacy;

import java.net.URI;

public final class LegacyJdbcUrls {

    private static final String JDBC_PREFIX = "jdbc:";
    public static final int DEFAULT_PORT = 5432;
    public static final String DEFAULT_DATABASE = "reestrdb";
    public static final String DEFAULT_USERNAME = "reestr_ro";

    private LegacyJdbcUrls() {
    }

    public static String build(String host, Integer port, String databaseName) {
        int resolvedPort = port == null ? DEFAULT_PORT : port;
        String database = databaseName == null || databaseName.isBlank() ? DEFAULT_DATABASE : databaseName;
        return "jdbc:postgresql://" + host + ":" + resolvedPort + "/" + database;
    }

    public static JdbcEndpoint parse(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return JdbcEndpoint.empty();
        }
        try {
            String withoutJdbc = jdbcUrl.startsWith(JDBC_PREFIX)
                    ? jdbcUrl.substring(JDBC_PREFIX.length())
                    : jdbcUrl;
            URI uri = URI.create(withoutJdbc);
            String host = uri.getHost();
            Integer port = uri.getPort() >= 0 ? uri.getPort() : DEFAULT_PORT;
            String path = uri.getPath();
            String database = null;
            if (path != null && path.length() > 1) {
                database = path.substring(1);
                int slash = database.indexOf('/');
                if (slash >= 0) {
                    database = database.substring(0, slash);
                }
                int query = database.indexOf('?');
                if (query >= 0) {
                    database = database.substring(0, query);
                }
            }
            return new JdbcEndpoint(host, port, database);
        } catch (Exception e) {
            return JdbcEndpoint.empty();
        }
    }

    public record JdbcEndpoint(String host, Integer port, String database) {
        static JdbcEndpoint empty() {
            return new JdbcEndpoint(null, null, null);
        }
    }
}
