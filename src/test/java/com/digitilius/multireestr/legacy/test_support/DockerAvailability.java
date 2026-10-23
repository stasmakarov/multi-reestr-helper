package com.digitilius.multireestr.legacy.test_support;

import org.testcontainers.DockerClientFactory;

public final class DockerAvailability {

    private DockerAvailability() {
    }

    public static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    public static boolean allowNoDocker() {
        return Boolean.parseBoolean(System.getProperty("allowNoDocker", "false"))
                || Boolean.parseBoolean(System.getenv("ALLOW_NO_DOCKER"));
    }
}
