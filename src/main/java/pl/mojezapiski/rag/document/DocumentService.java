package pl.mojezapiski.rag.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class DocumentService implements DocumentFacade {
    private final VectorStore pgVectorStore;
    @Override
    public void addDocuments(List<Document> documents) {
        pgVectorStore.add(documents);
        log.info("Added {} documents to the vector store", documents.size());
    }
    @Override
    public List<Document> getSimilarDocuments(String userPrompt) {
        return pgVectorStore.similaritySearch(SearchRequest.builder().query(userPrompt).topK(4).build());
    }
}
