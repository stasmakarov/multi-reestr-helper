package com.digitilius.multireestr.legacy.notice;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

final class LegacySqlLoader {

    private LegacySqlLoader() {
    }

    static String load(ResourceLoader resourceLoader, String location) {
        Resource resource = resourceLoader.getResource(location);
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить SQL: " + location, e);
        }
    }
}
