package io.upgradepilot.rag;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/** Internal API: not routed through the public gateway. */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    public record ExplainRequest(String question) {}
    public record ExplainResponse(String answer, List<String> sources) {}
    public record IngestResponse(int documents) {}

    private final MigrationAssistant assistant;
    private final IngestionService ingestionService;

    public RagController(MigrationAssistant assistant, IngestionService ingestionService) {
        this.assistant = assistant;
        this.ingestionService = ingestionService;
    }

    @PostMapping("/explain")
    public ExplainResponse explain(@RequestBody ExplainRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required");
        }
        Result<String> result = assistant.explain(request.question());
        List<String> sources = result.sources() == null ? List.of() : result.sources().stream()
                .map(Content::textSegment)
                .map(seg -> seg.metadata().getString("file_name"))
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return new ExplainResponse(result.content(), sources);
    }

    @PostMapping("/ingest")
    public IngestResponse ingest() {
        return new IngestResponse(ingestionService.reindex());
    }
}
