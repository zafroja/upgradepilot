package io.upgradepilot.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfig {

    /** all-MiniLM-L6-v2 produces 384-dimension vectors and runs in-process. */
    private static final int EMBEDDING_DIMENSION = 384;

    @Bean
    EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(
            @Value("${pgvector.host}") String host,
            @Value("${pgvector.port}") int port,
            @Value("${pgvector.database}") String database,
            @Value("${pgvector.user}") String user,
            @Value("${pgvector.password}") String password,
            @Value("${pgvector.table}") String table) {
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(user)
                .password(password)
                .table(table)
                .dimension(EMBEDDING_DIMENSION)
                .createTable(true)
                .build();
    }

    @Bean
    ChatLanguageModel chatModel(@Value("${anthropic.api-key}") String apiKey,
                                @Value("${anthropic.model}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set ANTHROPIC_API_KEY before starting rag-service");
        }
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(1024)
                .temperature(0.2)
                .build();
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel embeddingModel,
                                      @Value("${rag.max-results}") int maxResults,
                                      @Value("${rag.min-score}") double minScore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    @Bean
    MigrationAssistant migrationAssistant(ChatLanguageModel chatModel, ContentRetriever retriever) {
        return AiServices.builder(MigrationAssistant.class)
                .chatLanguageModel(chatModel)
                .contentRetriever(retriever)
                .build();
    }
}
