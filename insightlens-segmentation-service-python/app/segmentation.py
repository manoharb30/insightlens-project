import re
from typing import List, Tuple
import nltk
from nltk.tokenize import sent_tokenize
from loguru import logger
from sentence_transformers import SentenceTransformer
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

# Download required NLTK data
try:
    nltk.data.find('tokenizers/punkt')
except LookupError:
    nltk.download('punkt')

class TextSegmenter:
    def __init__(self, use_embeddings: bool = True):
        self.use_embeddings = use_embeddings
        if use_embeddings:
            self.model = SentenceTransformer('all-MiniLM-L6-v2')
            self.similarity_threshold = 0.85
            self.boundary_threshold = 0.3

    def _is_heading(self, line: str) -> bool:
        """Check if a line is a heading based on common patterns."""
        # Check for ALL CAPS
        if line.isupper() and len(line.strip()) > 3:
            return True
        
        # Check for common heading patterns
        heading_patterns = [
            r'^[IVX]+\.',  # Roman numerals
            r'^\d+\.',     # Numbers
            r'^[A-Z]\.',   # Single letters
            r'^Chapter\s+\d+',  # Chapter headings
            r'^Section\s+\d+',  # Section headings
        ]
        
        return any(re.match(pattern, line.strip()) for pattern in heading_patterns)

    def _split_by_headings(self, text: str) -> List[str]:
        """Split text into sections based on heading detection."""
        lines = text.split('\n')
        sections = []
        current_section = []
        
        for line in lines:
            if self._is_heading(line):
                if current_section:
                    sections.append('\n'.join(current_section))
                    current_section = []
            current_section.append(line)
        
        if current_section:
            sections.append('\n'.join(current_section))
        
        return sections

    def _semantic_segment_section(self, section: str) -> List[str]:
        """Segment a text section based on semantic similarity between sentences."""
        # Split into sentences
        sentences = sent_tokenize(section)
        if len(sentences) <= 1:
            return [section]

        # Generate embeddings for all sentences
        embeddings = self.model.encode(sentences)
        
        # Calculate similarity between adjacent sentences
        similarities = []
        for i in range(len(embeddings) - 1):
            similarity = cosine_similarity([embeddings[i]], [embeddings[i + 1]])[0][0]
            similarities.append(similarity)
        
        # Find segment boundaries
        segments = []
        current_segment = [sentences[0]]
        
        for i, similarity in enumerate(similarities):
            if similarity < self.boundary_threshold:
                # End of current segment
                segments.append(' '.join(current_segment))
                current_segment = [sentences[i + 1]]
            else:
                # Continue current segment
                current_segment.append(sentences[i + 1])
        
        # Add the last segment
        if current_segment:
            segments.append(' '.join(current_segment))
        
        return segments

    def _refine_with_embeddings(self, segments: List[str]) -> List[str]:
        """Refine segments using sentence embeddings and similarity."""
        if not segments:
            return segments

        # Get embeddings for all segments
        embeddings = self.model.encode(segments)
        
        # Compute similarity matrix
        similarity_matrix = cosine_similarity(embeddings)
        
        # Merge similar segments
        merged_segments = []
        i = 0
        while i < len(segments):
            current_segment = segments[i]
            j = i + 1
            
            while j < len(segments) and similarity_matrix[i, j] > self.similarity_threshold:
                current_segment += "\n" + segments[j]
                j += 1
            
            merged_segments.append(current_segment)
            i = j
        
        return merged_segments

    def segment_text(self, text: str) -> List[Tuple[int, str]]:
        """Main segmentation pipeline."""
        try:
            # Step 1: Split by headings
            heading_sections = self._split_by_headings(text)
            logger.info(f"Split text into {len(heading_sections)} heading sections")
            
            # Step 2: Apply semantic segmentation to each section
            all_segments = []
            for section in heading_sections:
                try:
                    semantic_segments = self._semantic_segment_section(section)
                    all_segments.extend(semantic_segments)
                except Exception as e:
                    logger.warning(f"Semantic segmentation failed for a section: {str(e)}")
                    all_segments.append(section)
            
            logger.info(f"Semantic segmentation produced {len(all_segments)} segments")
            
            # Step 3: Optional embedding-based refinement
            if self.use_embeddings:
                refined_segments = self._refine_with_embeddings(all_segments)
                logger.info(f"Embedding refinement produced {len(refined_segments)} segments")
                all_segments = refined_segments
            
            # Add segment order
            return [(i, segment) for i, segment in enumerate(all_segments)]
            
        except Exception as e:
            logger.error(f"Segmentation failed: {str(e)}")
            raise 