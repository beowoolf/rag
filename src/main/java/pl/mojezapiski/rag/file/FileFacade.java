package pl.mojezapiski.rag.file;

import org.springframework.web.multipart.MultipartFile;

interface FileFacade {
    void loadPdf(MultipartFile file);
}
