package com.digitilius.multireestr.legacy;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "legacy.datasource")
public class LegacyDataSourceProperties {

    private String url = "";
    private String username = "";
    private String password = "";
    private final Hikari hikari = new Hikari();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Hikari getHikari() {
        return hikari;
    }

    public static class Hikari {

        private boolean readOnly = true;
        private int maximumPoolSize = 5;
        private long connectionTimeout = 10_000;
        private long initializationFailTimeout = -1;

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public long getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public long getInitializationFailTimeout() {
            return initializationFailTimeout;
        }

        public void setInitializationFailTimeout(long initializationFailTimeout) {
            this.initializationFailTimeout = initializationFailTimeout;
        }
    }
}
