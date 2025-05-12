package com.insightlens.core.model;

/**
 * Enum representing the possible states of a document in the processing pipeline.
 */
public enum DocumentStatus {
    UPLOADED,                    // File is uploaded, no processing started
    TEXT_EXTRACTION_PENDING,     // Tika parsing and segmentation is about to start
    TEXT_EXTRACTION_IN_PROGRESS, // Tika parsing and segmentation is ongoing
    TEXT_EXTRACTION_COMPLETED,   // All text extracted and segmented successfully, segments saved
    TEXT_EXTRACTION_FAILED,      // Tika parsing or segmentation failed
    EMBEDDING_PENDING,           // Segments are ready for embedding (will be used later)
    EMBEDDING_COMPLETED,         // All segments embedded (will be used later)
    EMBEDDING_FAILED            // Embedding failed for one or more segments (will be used later)
} 