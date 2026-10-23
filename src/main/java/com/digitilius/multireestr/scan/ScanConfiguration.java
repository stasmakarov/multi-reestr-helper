package com.digitilius.multireestr.scan;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties(ScanProperties.class)
public class ScanConfiguration {

    @Bean
    Clock mtrClock(ScanProperties properties) {
        return Clock.system(ZoneId.of(properties.getZone()));
    }
}
