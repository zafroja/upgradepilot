package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Hystrix, Zuul and Ribbon were removed from Spring Cloud (great demo on PiggyMetrics). */
@Component
public class NetflixOssRule implements UpgradeRule {

    private static final Map<String, String> REPLACEMENTS = Map.of(
            "spring-cloud-starter-netflix-hystrix", "Resilience4j (spring-cloud-starter-circuitbreaker-resilience4j)",
            "spring-cloud-starter-netflix-hystrix-dashboard", "Micrometer + Prometheus/Grafana",
            "spring-cloud-starter-netflix-zuul", "Spring Cloud Gateway",
            "spring-cloud-starter-netflix-ribbon", "Spring Cloud LoadBalancer",
            "spring-cloud-netflix-turbine", "Micrometer + Prometheus/Grafana");

    @Override
    public List<Finding> check(ProjectContext ctx) {
        List<Finding> findings = new ArrayList<>();
        for (Path pom : ctx.pomFiles()) {
            List<String> lines = ctx.lines(pom);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                for (var e : REPLACEMENTS.entrySet()) {
                    if (line.contains("<artifactId>" + e.getKey() + "</artifactId>")) {
                        findings.add(new Finding("NETFLIX_OSS_REMOVED", Severity.BLOCKER,
                                e.getKey() + " is no longer supported",
                                ctx.relative(pom), i + 1,
                                "Replace with " + e.getValue() + ".",
                                "How do I migrate from " + e.getKey() + " to " + e.getValue() + " in Spring Cloud?"));
                    }
                }
            }
        }
        return findings;
    }
}
