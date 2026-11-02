package io.upgradepilot.rag;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;

public interface MigrationAssistant {

    @SystemMessage("""
            You are a senior Java engineer helping a team upgrade to Java 17 and Spring Boot 3.
            Answer using the provided migration notes. If the notes don't cover the question,
            say so briefly and give only well-established guidance.
            Format: a one-sentence summary, then numbered steps, then a short before/after
            code example when it helps. Keep it under 250 words.
            """)
    Result<String> explain(String question);
}
