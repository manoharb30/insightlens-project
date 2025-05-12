package com.insightlens.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TextSegmenterService {

    private static final int MAX_SEGMENT_LENGTH = 5000; // Maximum characters per segment
    private static final String PARAGRAPH_SEPARATOR = "\n\n";

    /**
     * Segments a text block into smaller chunks based on paragraphs and maximum length.
     * If a paragraph is too long, it will be split into smaller chunks.
     *
     * @param textBlock The text to segment
     * @return List of text segments
     */
    public List<String> segmentText(String textBlock) {
        if (textBlock == null || textBlock.isBlank()) {
            return List.of();
        }

        // Split by paragraphs
        List<String> paragraphs = Arrays.stream(textBlock.split(PARAGRAPH_SEPARATOR))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .collect(Collectors.toList());

        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();

        for (String paragraph : paragraphs) {
            // If adding this paragraph would exceed the max length, save current segment and start new one
            if (currentSegment.length() + paragraph.length() > MAX_SEGMENT_LENGTH) {
                if (currentSegment.length() > 0) {
                    segments.add(currentSegment.toString().trim());
                    currentSegment.setLength(0);
                }

                // If paragraph itself is too long, split it
                if (paragraph.length() > MAX_SEGMENT_LENGTH) {
                    segments.addAll(splitLongParagraph(paragraph));
                } else {
                    currentSegment.append(paragraph);
                }
            } else {
                if (currentSegment.length() > 0) {
                    currentSegment.append(PARAGRAPH_SEPARATOR);
                }
                currentSegment.append(paragraph);
            }
        }

        // Add the last segment if it has content
        if (currentSegment.length() > 0) {
            segments.add(currentSegment.toString().trim());
        }

        log.info("Segmented text into {} segments", segments.size());
        return segments;
    }

    /**
     * Splits a long paragraph into smaller chunks at sentence boundaries.
     * Falls back to character-based splitting if no sentence boundaries are found.
     */
    private List<String> splitLongParagraph(String paragraph) {
        List<String> chunks = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < paragraph.length()) {
            int endIndex = Math.min(startIndex + MAX_SEGMENT_LENGTH, paragraph.length());
            
            // Try to find a sentence boundary
            if (endIndex < paragraph.length()) {
                int lastPeriod = paragraph.lastIndexOf('.', endIndex);
                int lastQuestion = paragraph.lastIndexOf('?', endIndex);
                int lastExclamation = paragraph.lastIndexOf('!', endIndex);
                
                int lastSentenceEnd = Math.max(Math.max(lastPeriod, lastQuestion), lastExclamation);
                
                if (lastSentenceEnd > startIndex) {
                    endIndex = lastSentenceEnd + 1;
                }
            }

            chunks.add(paragraph.substring(startIndex, endIndex).trim());
            startIndex = endIndex;
        }

        return chunks;
    }
} 