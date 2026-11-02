package io.upgradepilot.scan.analysis.rules;

import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectContext;
import io.upgradepilot.scan.analysis.UpgradeRule;
import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Jakarta EE 9+ renamed javax.* packages used by Spring Boot 3. */
@Component
public class JavaxNamespaceRule implements UpgradeRule {

    private static final Pattern JAVAX_IMPORT = Pattern.compile(
            "^\\s*import\\s+(static\\s+)?javax\\.(persistence|servlet|validation|transaction|annotation\\.(PostConstruct|PreDestroy|Resource)|xml\\.bind|ws\\.rs)\\b.*");
    private static final int MAX_FILES_REPORTED = 200;

    @Override
    public List<Finding> check(ProjectContext ctx) {
        List<Finding> findings = new ArrayList<>();
        for (Path file : ctx.javaFiles()) {
            if (findings.size() >= MAX_FILES_REPORTED) break;
            List<String> lines = ctx.lines(file);
            int firstLine = -1;
            int count = 0;
            for (int i = 0; i < lines.size(); i++) {
                if (JAVAX_IMPORT.matcher(lines.get(i)).matches()) {
                    count++;
                    if (firstLine == -1) firstLine = i + 1;
                }
            }
            if (count > 0) {
                findings.add(new Finding("JAVAX_NAMESPACE", Severity.WARNING,
                        "javax imports must move to jakarta",
                        ctx.relative(file), firstLine,
                        count + " javax import(s) in this file.",
                        "How do I migrate javax.persistence, javax.servlet and javax.validation imports to jakarta for Spring Boot 3?"));
            }
        }
        return findings;
    }
}
