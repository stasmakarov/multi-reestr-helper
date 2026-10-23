package com.digitilius.multireestr.legacy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LegacyAvailabilityListener {

    private static final Logger log = LoggerFactory.getLogger(LegacyAvailabilityListener.class);

    private final LegacyConnectionService legacyConnectionService;
    private final LegacyDbInfo legacyDbInfo;

    public LegacyAvailabilityListener(
            LegacyConnectionService legacyConnectionService,
            LegacyDbInfo legacyDbInfo
    ) {
        this.legacyConnectionService = legacyConnectionService;
        this.legacyDbInfo = legacyDbInfo;
    }

    @EventListener
    @Order(0)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        legacyConnectionService.applyPersistedSettingsIfPresent();
        if (!legacyDbInfo.hasPool()) {
            log.warn("Legacy MultiReestr database is not configured. Application continues without it.");
            return;
        }
        try {
            Optional<String> version = legacyDbInfo.fetchPostgresVersion();
            if (version.isPresent()) {
                log.info("Connected to legacy MultiReestr database: {}", version.get());
            } else {
                log.warn("Legacy MultiReestr database is configured but not available. Application continues without it.");
            }
        } catch (Exception e) {
            log.warn("Legacy MultiReestr database is configured but not available: {}. Application continues without it.",
                    e.getMessage());
        }
    }
}
