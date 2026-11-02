package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringBootVersionRule implements UpgradeRule {

    @Override
    public List<Finding> check(ProjectContext ctx) {
        int major = ProjectContext.major(ctx.springBootVersion(), false);
        if (major == -1) {
            return List.of(new Finding("SPRING_BOOT_VERSION", Severity.INFO, "Spring Boot version not detected",
                    null, null, "No spring-boot-starter-parent or spring-boot.version property found.", null));
        }
        if (major < 3) {
            return List.of(new Finding("SPRING_BOOT_VERSION", Severity.BLOCKER,
                    "Upgrade Spring Boot " + ctx.springBootVersion() + " to 3.x",
                    null, null,
                    "Spring Boot 2.x is out of open-source support. Upgrade to 2.7 first, then to 3.x.",
                    "What is the recommended path to upgrade from Spring Boot " + ctx.springBootVersion()
                            + " to Spring Boot 3, and what are the main breaking changes?"));
        }
        return List.of();
    }
}
