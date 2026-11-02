package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** WebSecurityConfigurerAdapter was removed in Spring Security 6. */
@Component
public class WebSecurityAdapterRule implements UpgradeRule {

    @Override
    public List<Finding> check(ProjectContext ctx) {
        List<Finding> findings = new ArrayList<>();
        for (Path file : ctx.javaFiles()) {
            List<String> lines = ctx.lines(file);
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).contains("extends WebSecurityConfigurerAdapter")) {
                    findings.add(new Finding("WEB_SECURITY_ADAPTER", Severity.BLOCKER,
                            "WebSecurityConfigurerAdapter is removed in Spring Security 6",
                            ctx.relative(file), i + 1,
                            "Replace with a SecurityFilterChain @Bean.",
                            "How do I replace WebSecurityConfigurerAdapter with a SecurityFilterChain bean in Spring Security 6?"));
                    break;
                }
            }
        }
        return findings;
    }
}
