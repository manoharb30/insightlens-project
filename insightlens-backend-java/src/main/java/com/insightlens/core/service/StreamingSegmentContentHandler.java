package com.insightlens.core.service;

import com.insightlens.core.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class StreamingSegmentContentHandler extends DefaultHandler {
    private final Document document;
    private final StringBuilder currentTextBuffer;
    private final List<String> textChunks;
    private static final int CHARACTER_THRESHOLD_PER_CHUNK = 10000; // Kept for reference
    private static final Pattern MAJOR_BREAK_PATTERN = Pattern.compile("\\n\\s*\\n+");

    public StreamingSegmentContentHandler(Document document) {
        this.document = document;
        this.currentTextBuffer = new StringBuilder();
        this.textChunks = new ArrayList<>();
        log.debug("Initialized StreamingSegmentContentHandler for document: {}", document.getId());
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTextBuffer.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Add double newline for block-level elements to mark significant breaks
        if (isBlockLevelElement(localName)) {
            log.trace("Adding break marker after block-level element: {}", localName);
            currentTextBuffer.append("\n\n");
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Process any remaining text
        log.debug("Document parsing complete, processing final buffer ({} chars)", currentTextBuffer.length());
        processBufferedText();
        log.info("Completed document processing. Collected {} text chunks.", textChunks.size());
        log.debug("Total characters processed: {}", textChunks.stream()
                .mapToInt(String::length)
                .sum());
    }

    private boolean isBlockLevelElement(String localName) {
        return localName.equalsIgnoreCase("p") ||
               localName.equalsIgnoreCase("div") ||
               localName.equalsIgnoreCase("section") ||
               localName.equalsIgnoreCase("article") ||
               localName.equalsIgnoreCase("h1") ||
               localName.equalsIgnoreCase("h2") ||
               localName.equalsIgnoreCase("h3") ||
               localName.equalsIgnoreCase("h4") ||
               localName.equalsIgnoreCase("h5") ||
               localName.equalsIgnoreCase("h6") ||
               localName.equalsIgnoreCase("li");
    }

    private void processBufferedText() {
        if (currentTextBuffer.length() > 0) {
            String fullText = currentTextBuffer.toString();
            String[] potentialChunks = MAJOR_BREAK_PATTERN.split(fullText);
            
            for (String chunk : potentialChunks) {
                String trimmedChunk = chunk.trim();
                if (!trimmedChunk.isBlank()) {
                    log.trace("Adding text chunk of length {}", trimmedChunk.length());
                    textChunks.add(trimmedChunk);
                } else {
                    log.trace("Skipping empty text block");
                }
            }
            
            currentTextBuffer.setLength(0); // Clear the buffer
        }
    }

    public List<String> getTextChunks() {
        log.debug("Retrieved {} text chunks", textChunks.size());
        return textChunks;
    }
} 