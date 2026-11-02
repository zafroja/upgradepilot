package io.upgradepilot.scan.api;

import io.upgradepilot.scan.domain.Scan;

import java.time.Instant;
import java.util.UUID;

public record ScanSummary(UUID id, String projectName, String status,
                          String javaVersion, String springBootVersion, Instant createdAt) {
    public static ScanSummary from(Scan s) {
        return new ScanSummary(s.getId(), s.getProjectName(), s.getStatus().name(),
                s.getJavaVersion(), s.getSpringBootVersion(), s.getCreatedAt());
    }
}
