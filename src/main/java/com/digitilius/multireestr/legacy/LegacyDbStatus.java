package com.digitilius.multireestr.legacy;

public record LegacyDbStatus(
        boolean ok,
        String serverVersion,
        String currentUser,
        String database,
        String host,
        Integer port,
        boolean readOnly,
        long responseMs,
        String error,
        boolean privilegeWarning
) {
    public static final String NOT_CONFIGURED = "Legacy DataSource is not configured";

    public LegacyDbStatus withPrivilegeWarning(boolean warning) {
        return new LegacyDbStatus(
                ok, serverVersion, currentUser, database, host, port, readOnly, responseMs, error, warning
        );
    }
}
