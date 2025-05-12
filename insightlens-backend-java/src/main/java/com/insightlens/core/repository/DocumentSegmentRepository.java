package com.insightlens.core.repository;

import com.insightlens.core.model.DocumentSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSegmentRepository extends JpaRepository<DocumentSegment, String> {
} 