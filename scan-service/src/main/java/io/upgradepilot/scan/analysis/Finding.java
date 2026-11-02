package io.upgradepilot.scan.analysis;

import io.upgradepilot.scan.domain.Severity;

/**
 * One problem found by a rule.
 *
 * @param ragQuestion question sent to rag-service for an AI explanation (null = no explanation)
 */
public record Finding(
        String ruleId,
        Severity severity,
        String title,
        String filePath,
        Integer line,
        String detail,
        String ragQuestion) {
}
