package pl.mojezapiski.rag.document;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DocumentFacade {
    void addDocuments(List<Document> documents);

    List<Document> getSimilarDocuments(String userPrompt);
}
