package io.upgradepilot.scan.analysis;

import io.upgradepilot.scan.analysis.rules.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectAnalyzerTest {

    private final ProjectAnalyzer analyzer = new ProjectAnalyzer(List.of(
            new JavaVersionRule(), new SpringBootVersionRule(), new JavaxNamespaceRule(),
            new WebSecurityAdapterRule(), new SpringFactoriesRule(), new NetflixOssRule()));

    @Test
    void detectsLegacySpringBoot2Project(@TempDir Path root) throws Exception {
        Path project = root.resolve("legacy-app");
        Files.createDirectories(project.resolve("src/main/java/demo"));
        Files.writeString(project.resolve("pom.xml"), """
                <project>
                  <parent>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>2.7.18</version>
                  </parent>
                  <properties><java.version>11</java.version></properties>
                  <dependencies>
                    <dependency>
                      <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
                    </dependency>
                  </dependencies>
                </project>
                """);
        Files.writeString(project.resolve("src/main/java/demo/Account.java"), """
                package demo;
                import javax.persistence.Entity;
                @Entity class Account {}
                """);
        Files.writeString(project.resolve("src/main/java/demo/Security.java"), """
                package demo;
                class Security extends WebSecurityConfigurerAdapter {}
                """);

        AnalysisResult result = analyzer.analyze(root);

        assertThat(result.javaVersion()).isEqualTo("11");
        assertThat(result.springBootVersion()).isEqualTo("2.7.18");
        assertThat(result.findings()).extracting(Finding::ruleId).contains(
                "JAVA_VERSION", "SPRING_BOOT_VERSION", "JAVAX_NAMESPACE",
                "WEB_SECURITY_ADAPTER", "NETFLIX_OSS_REMOVED");
    }

    @Test
    void cleanSpringBoot3ProjectHasNoBlockers(@TempDir Path root) throws Exception {
        Files.writeString(root.resolve("pom.xml"), """
                <project>
                  <parent>
                    <artifactId>spring-boot-starter-parent</artifactId>
                    <version>3.3.4</version>
                  </parent>
                  <properties><java.version>17</java.version></properties>
                </project>
                """);

        AnalysisResult result = analyzer.analyze(root);

        assertThat(result.findings()).isEmpty();
    }
}
