package com.insightlens.core.service;

import com.insightlens.core.model.Document;
import com.insightlens.core.model.DocumentSegment;
import com.insightlens.core.repository.DocumentSegmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.UUID;

@Slf4j
public class StreamingSegmentContentHandler extends DefaultHandler {
    private final Document document;
    private final TextSegmenterService textSegmenterService;
    private final DocumentSegmentRepository documentSegmentRepository;
    private final StringBuilder currentTextBuffer;
    private int segmentOrderCounter;
    private static final int CHARACTER_THRESHOLD_PER_CHUNK = 10000; // ~3-4 pages of text

    public StreamingSegmentContentHandler(
            Document document,
            TextSegmenterService textSegmenterService,
            DocumentSegmentRepository documentSegmentRepository
    ) {
        this.document = document;
        this.textSegmenterService = textSegmenterService;
        this.documentSegmentRepository = documentSegmentRepository;
        this.currentTextBuffer = new StringBuilder();
        this.segmentOrderCounter = 0;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTextBuffer.append(ch, start, length);
        
        if (currentTextBuffer.length() >= CHARACTER_THRESHOLD_PER_CHUNK) {
            processAndClearBuffer();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Process text at paragraph boundaries
        if (isBlockLevelElement(localName)) {
            processAndClearBuffer();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Process any remaining text
        processAndClearBuffer();
        log.info("Completed document processing. Created {} segments.", segmentOrderCounter);
    }

    private boolean isBlockLevelElement(String localName) {
        return localName.equalsIgnoreCase("p") ||
               localName.equalsIgnoreCase("div") ||
               localName.equalsIgnoreCase("section") ||
               localName.equalsIgnoreCase("article");
    }

    private void processAndClearBuffer() {
        if (currentTextBuffer.length() > 0) {
            try {
                String textBlock = currentTextBuffer.toString();
                textSegmenterService.segmentText(textBlock).stream()
                        .filter(segment -> !segment.isBlank())
                        .forEach(segment -> {
                            DocumentSegment documentSegment = DocumentSegment.builder()
                                    .id(UUID.randomUUID().toString())
                                    .document(document)
                                    .segmentText(segment)
                                    .segmentOrder(segmentOrderCounter++)
                                    .build();
                            documentSegmentRepository.save(documentSegment);
                        });
            } catch (Exception e) {
                log.error("Error processing text buffer: {}", e.getMessage(), e);
            } finally {
                currentTextBuffer.setLength(0); // Clear the buffer
            }
        }
    }
} 