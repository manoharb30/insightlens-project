package com.insightlens.core.controller;

import com.insightlens.core.model.Document;
import com.insightlens.core.model.DocumentStatus;
import com.insightlens.core.repository.DocumentRepository;
import com.insightlens.core.service.AsyncDocumentProcessorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@Slf4j
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final AsyncDocumentProcessorService asyncDocumentProcessorService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory: {}", uploadDir, e);
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        try {
            String documentId = UUID.randomUUID().toString();
            String originalFilename = file.getOriginalFilename();
            String storedFilePath = Paths.get(uploadDir, documentId + "_" + originalFilename).toString();

            // Save the file
            file.transferTo(Paths.get(storedFilePath));

            // Create and save document entity
            Document document = Document.builder()
                    .id(documentId)
                    .originalFileName(originalFilename)
                    .storedFilePath(storedFilePath)
                    .fileContentType(file.getContentType())
                    .fileSize(file.getSize())
                    .status(DocumentStatus.UPLOADED)
                    .build();

            documentRepository.save(document);

            // Start async processing
            asyncDocumentProcessorService.processUploadedDocument(documentId);

            return ResponseEntity.accepted().body(Map.of(
                    "documentId", documentId,
                    "message", "File upload accepted. Processing initiated."
            ));

        } catch (IOException e) {
            log.error("Failed to process file upload", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process file upload"));
        }
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<Map<String, Object>> getDocumentStatus(@PathVariable String documentId) {
        return documentRepository.findById(documentId)
                .map(document -> {
                    Map<String, Object> response = Map.of(
                            "documentId", document.getId(),
                            "status", document.getStatus(),
                            "statusMessage", document.getStatusMessage(),
                            "lastUpdated", document.getUpdatedAt()
                    );
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 