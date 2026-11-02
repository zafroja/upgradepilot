package io.upgradepilot.scan.api;

import io.upgradepilot.scan.analysis.AnalysisResult;
import io.upgradepilot.scan.analysis.Finding;
import io.upgradepilot.scan.analysis.ProjectAnalyzer;
import io.upgradepilot.scan.analysis.ZipExtractor;
import io.upgradepilot.scan.domain.Issue;
import io.upgradepilot.scan.domain.Scan;
import io.upgradepilot.scan.domain.ScanRepository;
import io.upgradepilot.scan.rag.RagClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ScanService {

    private static final Logger log = LoggerFactory.getLogger(ScanService.class);

    private final ScanRepository repository;
    private final ProjectAnalyzer analyzer;
    private final RagClient ragClient;
    private final int maxExplanations;

    public ScanService(ScanRepository repository, ProjectAnalyzer analyzer, RagClient ragClient,
                       @Value("${upgradepilot.rag.max-explanations:5}") int maxExplanations) {
        this.repository = repository;
        this.analyzer = analyzer;
        this.ragClient = ragClient;
        this.maxExplanations = maxExplanations;
    }

    /**
     * Synchronous for the MVP. Next step: return 202 + scan id and process
     * on a queue (Kafka or SQS) so large repos don't hold the HTTP request.
     */
    public ScanResponse createScan(String projectName, MultipartFile file) {
        Scan scan = repository.save(new Scan(projectName));
        Path workDir = null;
        try (InputStream in = file.getInputStream()) {
            workDir = Files.createTempDirectory("upgradepilot-");
            ZipExtractor.extract(in, workDir);

            AnalysisResult result = analyzer.analyze(workDir);
            Map<String, String> explanations = explain(result.findings());

            for (Finding f : result.findings()) {
                String explanation = f.ragQuestion() == null ? null : explanations.get(f.ragQuestion());
                scan.addIssue(new Issue(f, explanation));
            }
            scan.complete(result.javaVersion(), result.springBootVersion());
        } catch (Exception e) {
            log.error("Scan {} failed", scan.getId(), e);
            scan.fail(e.getMessage());
        } finally {
            deleteQuietly(workDir);
        }
        repository.save(scan);
        return getScan(scan.getId());
    }

    public ScanResponse getScan(UUID id) {
        return repository.findWithIssuesById(id)
                .map(ScanResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Scan not found"));
    }

    public List<ScanSummary> recentScans() {
        return repository.findTop20ByOrderByCreatedAtDesc().stream().map(ScanSummary::from).toList();
    }

    /** One LLM call per distinct question, most severe first, capped to control cost. */
    private Map<String, String> explain(List<Finding> findings) {
        Map<String, String> answers = new HashMap<>();
        findings.stream()
                .filter(f -> f.ragQuestion() != null)
                .sorted(Comparator.comparingInt(f -> f.severity().ordinal()))
                .map(Finding::ragQuestion)
                .distinct()
                .limit(maxExplanations)
                .forEach(q -> ragClient.explain(q).ifPresent(a -> answers.put(q, a)));
        return answers;
    }

    private static void deleteQuietly(Path dir) {
        if (dir == null) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // temp dir; OS will clean up eventually
                }
            });
        } catch (IOException ignored) {
        }
    }
}
