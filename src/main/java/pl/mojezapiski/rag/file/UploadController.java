package pl.mojezapiski.rag.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/upload")
class UploadController {
    private final FileFacade fileFacade;
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void upload(@RequestParam("file") MultipartFile file) {
        fileFacade.loadPdf(file);
    }
}
