package io.upgradepilot.scan.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/** Calls the internal rag-service. A RAG failure never fails the scan. */
@Component
public class RagClient {

    private static final Logger log = LoggerFactory.getLogger(RagClient.class);

    record ExplainRequest(String question) {}
    record ExplainResponse(String answer, List<String> sources) {}

    private final RestClient client;
    private final boolean enabled;

    public RagClient(RestClient.Builder builder,
                     @Value("${upgradepilot.rag.base-url}") String baseUrl,
                     @Value("${upgradepilot.rag.enabled:true}") boolean enabled) {
        this.client = builder.baseUrl(baseUrl).build();
        this.enabled = enabled;
    }

    public Optional<String> explain(String question) {
        if (!enabled) return Optional.empty();
        try {
            ExplainResponse r = client.post()
                    .uri("/api/rag/explain")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ExplainRequest(question))
                    .retrieve()
                    .body(ExplainResponse.class);
            if (r == null || r.answer() == null) return Optional.empty();
            String sources = (r.sources() == null || r.sources().isEmpty())
                    ? "" : "\n\nSources: " + String.join(", ", r.sources());
            return Optional.of(r.answer() + sources);
        } catch (RestClientException e) {
            log.warn("RAG explanation failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
