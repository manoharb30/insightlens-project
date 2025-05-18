package com.insightlens.core.service;

import com.insightlens.core.model.DocumentSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SegmentationServiceClient {
    private final WebClient webClient;

    @Value("${segmentation.service.url}")
    private String segmentationServiceUrl;

    public List<DocumentSegment> segmentText(String documentId, String text) {
        log.info("Sending text to segmentation service for document: {}", documentId);
        log.debug("Segmentation service URL: {}", segmentationServiceUrl);
        log.debug("Text length to segment: {} characters", text.length());

        SegmentationRequest request = new SegmentationRequest(documentId, text);
        
        try {
            log.debug("Making POST request to {}/segment", segmentationServiceUrl);
            return webClient.post()
                    .uri(segmentationServiceUrl + "/segment")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SegmentationResponse.class)
                    .map(response -> {
                        log.debug("Received segmentation response with {} segments", response.segments().size());
                        return response.segments().stream()
                                .map(segment -> {
                                    log.trace("Processing segment {} with length {}", 
                                            segment.segmentOrder(), 
                                            segment.text().length());
                                    return DocumentSegment.builder()
                                            .id(UUID.randomUUID().toString())
                                            .segmentOrder(segment.segmentOrder())
                                            .segmentText(segment.text())
                                            .build();
                                })
                                .toList();
                    })
                    .onErrorResume(e -> {
                        log.error("Error calling segmentation service: {}", e.getMessage(), e);
                        log.debug("Request details - URL: {}, DocumentId: {}, Text length: {}", 
                                segmentationServiceUrl, documentId, text.length());
                        return Mono.error(new RuntimeException("Failed to segment text: " + e.getMessage()));
                    })
                    .block();
        } catch (Exception e) {
            log.error("Unexpected error during segmentation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to segment text: " + e.getMessage());
        }
    }

    private record SegmentationRequest(String documentId, String text) {}
    private record TextSegment(int segmentOrder, String text) {}
    private record SegmentationResponse(List<TextSegment> segments) {}
} 