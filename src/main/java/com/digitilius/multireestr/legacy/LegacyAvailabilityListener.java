package com.digitilius.multireestr.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LegacyAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(LegacyAvailabilityListener.class);

    private final Environment environment;
    private final LegacyDbInfo legacyDbInfo;

    @Autowired
    public LegacyAvailabilityListener(Environment environment, LegacyDbInfo legacyDbInfo) {
        this.environment = environment;
        this.legacyDbInfo = legacyDbInfo;
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        String url = environment.getProperty("legacy.datasource.url", "");
        if (url.isBlank()) {
            log.warn("Legacy MultiReestr database is not configured. Application continues without it.");
            return;
        }
        try {
            Optional<String> version = legacyDbInfo.fetchPostgresVersion();
            if (version.isPresent()) {
                log.info("Connected to legacy MultiReestr database: {}", version.get());
            } else {
                log.warn("Legacy MultiReestr database is configured but JDBC is not available. Application continues without it.");
            }
        } catch (Exception e) {
            log.warn("Legacy MultiReestr database is configured but not available: {}. Application continues without it.",
                    e.getMessage());
        }
    }
}
