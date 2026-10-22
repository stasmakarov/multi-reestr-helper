package com.digitilius.multireestr.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AppVersionService {

    public static final String FALLBACK_VERSION = "0.0.1-SNAPSHOT";

    private final ObjectProvider<BuildProperties> buildProperties;
    private final ObjectProvider<GitProperties> gitProperties;

    public AppVersionService(ObjectProvider<BuildProperties> buildProperties,
                             ObjectProvider<GitProperties> gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    public Info current() {
        BuildProperties build = buildProperties.getIfAvailable();
        GitProperties git = gitProperties.getIfAvailable();

        String version = build != null && hasText(build.getVersion())
                ? build.getVersion()
                : FALLBACK_VERSION;
        String commit = git != null ? git.getShortCommitId() : null;
        String branch = git != null ? git.getBranch() : null;
        Instant date = null;
        if (git != null) {
            date = git.getCommitTime();
        }
        if (date == null && build != null) {
            date = build.getTime();
        }
        return new Info(version, blankToNull(commit), blankToNull(branch), date);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return hasText(value) ? value : null;
    }

    public record Info(String version, String build, String branch, Instant date) {
    }
}
