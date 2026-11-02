package io.upgradepilot.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/** Loads migration notes (.md / .txt) from docs-path, chunks, embeds and stores them in pgvector. */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final EmbeddingStore<TextSegment> store;
    private final EmbeddingModel embeddingModel;
    private final Path docsPath;

    public IngestionService(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel,
                            @Value("${rag.docs-path}") String docsPath) {
        this.store = store;
        this.embeddingModel = embeddingModel;
        this.docsPath = Path.of(docsPath);
    }

    public int reindex() {
        try {
            store.removeAll(); // avoid duplicate chunks on re-ingest
        } catch (UnsupportedOperationException e) {
            log.warn("Embedding store can't clear itself; drop the table manually before re-ingesting");
        }

        List<Document> docs = FileSystemDocumentLoader.loadDocuments(docsPath, new TextDocumentParser());
        EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(800, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .build()
                .ingest(docs);
        log.info("Ingested {} documents from {}", docs.size(), docsPath.toAbsolutePath());
        return docs.size();
    }
}
