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
        String error
) {
}
