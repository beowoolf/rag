package pl.mojezapiski.rag.file;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.mojezapiski.rag.document.DocumentFacade;

import java.util.List;

@Service
@RequiredArgsConstructor
class FileService implements FileFacade {
    private final DocumentFacade documentFacade;
    private PdfDocumentReaderConfig createReaderConfig() {
        return PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();
    }
    @Override
    public void loadPdf(MultipartFile file) {
        if ("application/pdf".equals(file.getContentType())) {
            PagePdfDocumentReader documentReader = new PagePdfDocumentReader(file.getResource(), createReaderConfig());

            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> documents = textSplitter.apply(documentReader.get());

            documentFacade.addDocuments(documents);
        }
    }
}
