package com.digitilius.multireestr.legacy;

import com.digitilius.multireestr.entity.LegacyConnectionSettings;
import com.zaxxer.hikari.HikariDataSource;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class LegacyConnectionService {

    private static final Logger log = LoggerFactory.getLogger(LegacyConnectionService.class);

    public enum Source {
        DATABASE,
        PROPERTIES,
        NONE
    }

    public record FormValues(
            boolean enabled,
            String host,
            Integer port,
            String databaseName,
            String username,
            String password
    ) {
    }

    public record ScreenModel(LegacyConnectionSettings settings, Source source) {
    }

    private final DataManager dataManager;
    private final UnconstrainedDataManager unconstrainedDataManager;
    private final LegacyDataSourceProperties properties;
    private final LegacyHikariFactory factory;
    private final SwappableLegacyDataSource legacyDataSource;
    private final LegacySecretCipher cipher;
    private final LegacyDbInfo legacyDbInfo;

    public LegacyConnectionService(
            DataManager dataManager,
            UnconstrainedDataManager unconstrainedDataManager,
            LegacyDataSourceProperties properties,
            LegacyHikariFactory factory,
            @Qualifier("legacy") SwappableLegacyDataSource legacyDataSource,
            LegacySecretCipher cipher,
            LegacyDbInfo legacyDbInfo
    ) {
        this.dataManager = dataManager;
        this.unconstrainedDataManager = unconstrainedDataManager;
        this.properties = properties;
        this.factory = factory;
        this.legacyDataSource = legacyDataSource;
        this.cipher = cipher;
        this.legacyDbInfo = legacyDbInfo;
    }

    public ScreenModel loadForScreen() {
        Optional<LegacyConnectionSettings> stored = findDefault();
        if (stored.isPresent()) {
            return new ScreenModel(stored.get(), Source.DATABASE);
        }
        LegacyConnectionSettings created = dataManager.create(LegacyConnectionSettings.class);
        created.setCode(LegacyConnectionSettings.DEFAULT_CODE);
        fillFromProperties(created);
        created.setEnabled(hasPropertiesUrl());
        return new ScreenModel(created, hasPropertiesUrl() ? Source.PROPERTIES : Source.NONE);
    }

    public LegacyDbStatus testFromForm(FormValues form) {
        String password = resolvePassword(form.password(), findDefault().orElse(null));
        LegacyJdbcUrls.JdbcEndpoint endpoint = new LegacyJdbcUrls.JdbcEndpoint(
                form.host(), form.port(), form.databaseName()
        );
        HikariDataSource probe = factory.create(
                form.host(), form.port(), form.databaseName(), form.username(), password, properties
        );
        try {
            return legacyDbInfo.check(new NamedParameterJdbcTemplate(probe), endpoint);
        } finally {
            probe.close();
        }
    }

    public LegacyDbStatus saveAndApply(LegacyConnectionSettings settings, String typedPassword) {
        if (!cipher.isConfigured()) {
            throw new IllegalStateException("MTR_SECRET_KEY / mtr.secret-key is not set");
        }
        settings.setCode(LegacyConnectionSettings.DEFAULT_CODE);
        String password = resolvePassword(typedPassword, settings);
        if (Boolean.TRUE.equals(settings.getEnabled()) && (password == null || password.isBlank())
                && isBlank(settings.getPasswordEncrypted())) {
            throw new IllegalStateException("Password is required");
        }
        if (typedPassword != null && !typedPassword.isBlank()) {
            settings.setPasswordEncrypted(cipher.encrypt(typedPassword));
        } else if (isBlank(settings.getPasswordEncrypted()) && password != null && !password.isBlank()) {
            settings.setPasswordEncrypted(cipher.encrypt(password));
        }

        applyPool(settings, password);
        LegacyDbStatus status = legacyDbInfo.check();
        applyCheckResult(settings, status);
        return status;
    }

    public LegacyDbStatus resetToProperties() {
        Optional<LegacyConnectionSettings> stored = findDefault();
        if (stored.isPresent()) {
            LegacyConnectionSettings settings = stored.get();
            settings.setEnabled(false);
            applyPoolFromProperties();
            LegacyDbStatus status = legacyDbInfo.check();
            applyCheckResult(settings, status);
            return status;
        }
        applyPoolFromProperties();
        return legacyDbInfo.check();
    }

    public void applyPersistedSettingsIfPresent() {
        Optional<LegacyConnectionSettings> stored;
        try {
            stored = findDefaultUnconstrained();
        } catch (Exception e) {
            log.warn("Cannot read LegacyConnectionSettings: {}", e.getMessage());
            return;
        }
        if (stored.isEmpty() || !Boolean.TRUE.equals(stored.get().getEnabled())) {
            return;
        }
        LegacyConnectionSettings settings = stored.get();
        if (!cipher.isConfigured()) {
            log.warn("LegacyConnectionSettings is enabled but MTR_SECRET_KEY is not set. Using properties.");
            return;
        }
        try {
            String password = cipher.decrypt(settings.getPasswordEncrypted());
            applyPool(settings, password);
        } catch (Exception e) {
            log.warn("Cannot decrypt LegacyConnectionSettings password. Using properties. {}", e.getMessage());
        }
    }

    public boolean secretKeyConfigured() {
        return cipher.isConfigured();
    }

    public void applyRuntime(
            String host,
            Integer port,
            String databaseName,
            String username,
            String password
    ) {
        legacyDataSource.swap(factory.create(host, port, databaseName, username, password, properties));
    }

    private void applyPool(LegacyConnectionSettings settings, String password) {
        if (!Boolean.TRUE.equals(settings.getEnabled())) {
            applyPoolFromProperties();
            return;
        }
        legacyDataSource.swap(factory.create(
                settings.getHost(),
                settings.getPort(),
                settings.getDatabaseName(),
                settings.getUsername(),
                password,
                properties
        ));
    }

    private void applyPoolFromProperties() {
        if (!hasPropertiesUrl()) {
            legacyDataSource.swap(null);
            return;
        }
        legacyDataSource.swap(factory.create(
                properties.getUrl(),
                properties.getUsername(),
                properties.getPassword(),
                properties
        ));
    }

    private void applyCheckResult(LegacyConnectionSettings settings, LegacyDbStatus status) {
        settings.setLastCheckAt(OffsetDateTime.now());
        settings.setLastCheckOk(status.ok());
        String message = status.ok() ? status.serverVersion() : status.error();
        if (message != null && message.length() > 1000) {
            message = message.substring(0, 1000);
        }
        settings.setLastCheckMessage(message);
        dataManager.save(new SaveContext().saving(settings));
    }

    private Optional<LegacyConnectionSettings> findDefault() {
        return dataManager.load(LegacyConnectionSettings.class)
                .query("select e from mtr_LegacyConnectionSettings e where e.code = :code")
                .parameter("code", LegacyConnectionSettings.DEFAULT_CODE)
                .optional();
    }

    private Optional<LegacyConnectionSettings> findDefaultUnconstrained() {
        return unconstrainedDataManager.load(LegacyConnectionSettings.class)
                .query("select e from mtr_LegacyConnectionSettings e where e.code = :code")
                .parameter("code", LegacyConnectionSettings.DEFAULT_CODE)
                .optional();
    }

    private String resolvePassword(String typedPassword, LegacyConnectionSettings settings) {
        if (typedPassword != null && !typedPassword.isBlank()) {
            return typedPassword;
        }
        if (settings != null && !isBlank(settings.getPasswordEncrypted()) && cipher.isConfigured()) {
            try {
                return cipher.decrypt(settings.getPasswordEncrypted());
            } catch (Exception e) {
                log.warn("Cannot decrypt stored legacy password");
            }
        }
        return properties.getPassword();
    }

    private void fillFromProperties(LegacyConnectionSettings settings) {
        LegacyJdbcUrls.JdbcEndpoint endpoint = LegacyJdbcUrls.parse(properties.getUrl());
        settings.setHost(endpoint.host() != null ? endpoint.host() : "localhost");
        settings.setPort(endpoint.port() != null ? endpoint.port() : LegacyJdbcUrls.DEFAULT_PORT);
        settings.setDatabaseName(endpoint.database() != null ? endpoint.database() : LegacyJdbcUrls.DEFAULT_DATABASE);
        String username = properties.getUsername();
        settings.setUsername(username == null || username.isBlank() ? LegacyJdbcUrls.DEFAULT_USERNAME : username);
    }

    private boolean hasPropertiesUrl() {
        return properties.getUrl() != null && !properties.getUrl().isBlank();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
