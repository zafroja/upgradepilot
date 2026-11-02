package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.util.List;

/** Boot 3 no longer reads auto-configurations from spring.factories. */
@Component
public class SpringFactoriesRule implements UpgradeRule {

    @Override
    public List<Finding> check(ProjectContext ctx) {
        return ctx.files().stream()
                .filter(p -> p.getFileName().toString().equals("spring.factories"))
                .filter(p -> ctx.content(p).contains("EnableAutoConfiguration"))
                .map(p -> new Finding("SPRING_FACTORIES_AUTOCONFIG", Severity.WARNING,
                        "Auto-configuration registered in spring.factories",
                        ctx.relative(p), null,
                        "Move entries to META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports.",
                        "How do I register auto-configuration classes in Spring Boot 3 instead of spring.factories?"))
                .toList();
    }
}
