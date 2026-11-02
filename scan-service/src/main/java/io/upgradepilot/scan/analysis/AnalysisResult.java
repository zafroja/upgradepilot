package io.upgradepilot.scan.analysis;

import java.util.List;

public record AnalysisResult(String javaVersion, String springBootVersion, List<Finding> findings) {
}
