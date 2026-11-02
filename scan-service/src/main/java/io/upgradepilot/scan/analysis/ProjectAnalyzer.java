package io.upgradepilot.scan.analysis;

import io.upgradepilot.scan.domain.Severity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class ProjectAnalyzer {

    private static final List<String> IGNORED_DIRS = List.of("/target/", "/.git/", "/node_modules/");

    private final List<UpgradeRule> rules;

    public ProjectAnalyzer(List<UpgradeRule> rules) {
        this.rules = rules;
    }

    public AnalysisResult analyze(Path root) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(Files::isRegularFile)
                    .filter(p -> IGNORED_DIRS.stream().noneMatch(d -> ("/" + root.relativize(p).toString().replace('\\', '/')).contains(d)))
                    .toList();
        }

        // Shallowest pom first = the root/parent pom (GitHub zips add a top-level folder).
        List<Path> poms = files.stream()
                .filter(p -> p.getFileName().toString().equals("pom.xml"))
                .sorted(Comparator.comparingInt(Path::getNameCount))
                .toList();

        List<PomReader.PomInfo> infos = poms.stream()
                .map(PomReader::read)
                .flatMap(java.util.Optional::stream)
                .toList();

        String javaVersion = infos.stream().map(PomReader.PomInfo::javaVersion)
                .filter(Objects::nonNull).findFirst().orElse(null);
        String bootVersion = infos.stream().map(PomReader.PomInfo::springBootVersion)
                .filter(Objects::nonNull).findFirst().orElse(null);

        ProjectContext ctx = new ProjectContext(root, files, poms, javaVersion, bootVersion);

        List<Finding> findings = new ArrayList<>();
        if (poms.isEmpty()) {
            findings.add(new Finding("NO_MAVEN_BUILD", Severity.INFO,
                    "No pom.xml found",
                    null, null,
                    "Only Maven projects are supported right now. Gradle support is on the roadmap.",
                    null));
        }
        for (UpgradeRule rule : rules) {
            findings.addAll(rule.check(ctx));
        }
        return new AnalysisResult(javaVersion, bootVersion, findings);
    }
}
