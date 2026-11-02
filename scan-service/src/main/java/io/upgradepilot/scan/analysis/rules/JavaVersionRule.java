package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JavaVersionRule implements UpgradeRule {

    @Override
    public List<Finding> check(ProjectContext ctx) {
        int major = ProjectContext.major(ctx.javaVersion(), true);
        if (major == -1) {
            return List.of(new Finding("JAVA_VERSION", Severity.INFO, "Java version not declared",
                    null, null, "Set java.version or maven.compiler.release in the root pom.xml.", null));
        }
        if (major < 17) {
            return List.of(new Finding("JAVA_VERSION", Severity.BLOCKER,
                    "Upgrade Java " + major + " to 17 or later",
                    null, null,
                    "Spring Boot 3 requires Java 17+. Current: " + ctx.javaVersion(),
                    "What changes are needed to move a Spring Boot application from Java " + major
                            + " to Java 17, and what commonly breaks?"));
        }
        return List.of();
    }
}
