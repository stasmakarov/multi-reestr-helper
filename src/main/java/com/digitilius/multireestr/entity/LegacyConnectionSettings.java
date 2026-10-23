package com.digitilius.multireestr.entity;

import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;
import java.util.UUID;

@JmixEntity
@Table(name = "MTR_LEGACY_CONNECTION_SETTINGS", indexes = {
        @Index(name = "IDX_MTR_LEGACY_CONN_SETTINGS_UNQ", columnList = "CODE", unique = true)
})
@Entity(name = "mtr_LegacyConnectionSettings")
public class LegacyConnectionSettings {

    public static final String DEFAULT_CODE = "DEFAULT";

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @Column(name = "ENABLED", nullable = false)
    private Boolean enabled;

    @Column(name = "HOST", nullable = false)
    private String host;

    @Column(name = "PORT", nullable = false)
    private Integer port;

    @Column(name = "DATABASE_NAME", nullable = false, length = 100)
    private String databaseName;

    @Column(name = "USERNAME", nullable = false, length = 100)
    private String username;

    @Secret
    @SystemLevel
    @Column(name = "PASSWORD_ENCRYPTED", length = 1000)
    private String passwordEncrypted;

    @Column(name = "LAST_CHECK_AT")
    private OffsetDateTime lastCheckAt;

    @Column(name = "LAST_CHECK_OK")
    private Boolean lastCheckOk;

    @Column(name = "LAST_CHECK_MESSAGE", length = 1000)
    private String lastCheckMessage;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public OffsetDateTime getLastCheckAt() {
        return lastCheckAt;
    }

    public void setLastCheckAt(OffsetDateTime lastCheckAt) {
        this.lastCheckAt = lastCheckAt;
    }

    public Boolean getLastCheckOk() {
        return lastCheckOk;
    }

    public void setLastCheckOk(Boolean lastCheckOk) {
        this.lastCheckOk = lastCheckOk;
    }

    public String getLastCheckMessage() {
        return lastCheckMessage;
    }

    public void setLastCheckMessage(String lastCheckMessage) {
        this.lastCheckMessage = lastCheckMessage;
    }

    @InstanceName
    @DependsOnProperties({"host", "port", "databaseName"})
    public String getInstanceName() {
        String hostValue = host != null ? host : "";
        String portValue = port != null ? String.valueOf(port) : "";
        String database = databaseName != null ? databaseName : "";
        return hostValue + ":" + portValue + "/" + database;
    }
}
