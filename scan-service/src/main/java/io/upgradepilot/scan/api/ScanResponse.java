package io.upgradepilot.scan.api;

import io.upgradepilot.scan.domain.Issue;
import io.upgradepilot.scan.domain.Scan;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ScanResponse(
        UUID id,
        String projectName,
        String status,
        String javaVersion,
        String springBootVersion,
        Instant createdAt,
        String errorMessage,
        Map<String, Long> counts,
        List<IssueResponse> issues) {

    public record IssueResponse(String ruleId, String severity, String title, String filePath,
                                Integer line, String detail, String explanation) {
        static IssueResponse from(Issue i) {
            return new IssueResponse(i.getRuleId(), i.getSeverity().name(), i.getTitle(), i.getFilePath(),
                    i.getLineNumber(), i.getDetail(), i.getExplanation());
        }
    }

    public static ScanResponse from(Scan s) {
        List<Issue> sorted = s.getIssues().stream()
                .sorted(Comparator.comparing((Issue i) -> i.getSeverity().ordinal())
                        .thenComparing(i -> i.getFilePath() == null ? "" : i.getFilePath()))
                .toList();

        Map<String, Long> counts = new LinkedHashMap<>();
        for (Issue i : sorted) counts.merge(i.getSeverity().name(), 1L, Long::sum);

        return new ScanResponse(s.getId(), s.getProjectName(), s.getStatus().name(),
                s.getJavaVersion(), s.getSpringBootVersion(), s.getCreatedAt(), s.getErrorMessage(),
                counts, sorted.stream().map(IssueResponse::from).toList());
    }
}
