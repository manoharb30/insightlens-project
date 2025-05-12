package com.insightlens.core.service;

import com.insightlens.core.model.Document;
import com.insightlens.core.model.DocumentStatus;
import com.insightlens.core.repository.DocumentRepository;
import com.insightlens.core.repository.DocumentSegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncDocumentProcessorService {

    private final DocumentRepository documentRepository;
    private final TextSegmenterService textSegmenterService;
    private final DocumentSegmentRepository documentSegmentRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Async("documentProcessingExecutor")
    @Transactional
    public Future<Void> processUploadedDocument(String documentId) {
        log.info("Starting document processing for documentId: {}", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        try {
            // Update status to processing
            document.setStatus(DocumentStatus.TEXT_EXTRACTION_IN_PROGRESS);
            documentRepository.save(document);

            // Initialize Tika components
            Parser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();

            // Create our custom streaming handler
            StreamingSegmentContentHandler segmentHandler = new StreamingSegmentContentHandler(
                    document,
                    textSegmenterService,
                    documentSegmentRepository
            );

            File file = new File(document.getStoredFilePath());
            try (InputStream stream = new FileInputStream(file)) {
                // Parse the document using our streaming handler
                parser.parse(stream, segmentHandler, metadata, context);
                
                // Update document status
                document.setStatus(DocumentStatus.TEXT_EXTRACTION_COMPLETED);
                document.setStatusMessage("Text extraction and segmentation completed successfully");
                documentRepository.save(document);
                
            } catch (IOException | SAXException | TikaException e) {
                log.error("Error during Tika parsing or segmentation for document {}: {}", documentId, e.getMessage(), e);
                document.setStatus(DocumentStatus.TEXT_EXTRACTION_FAILED);
                document.setStatusMessage("Tika parsing/segmentation failed: " + e.getMessage());
                documentRepository.save(document);
            }

        } catch (Exception e) {
            log.error("Unexpected error processing document {}: {}", documentId, e.getMessage(), e);
            document.setStatus(DocumentStatus.TEXT_EXTRACTION_FAILED);
            document.setStatusMessage("Unexpected error during processing: " + e.getMessage());
            documentRepository.save(document);
        }

         return CompletableFuture.completedFuture(null);
    }
} 