import spacy
from sentence_transformers import SentenceTransformer
import nltk
from typing import List, Dict, Tuple
import re
from dataclasses import dataclass
from datetime import datetime
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class Segment:
    text: str
    start_index: int
    end_index: int
    segment_type: str
    metadata: Dict = None

class SemanticSegmenter:
    def __init__(self):
        """Initialize the semantic segmenter with required models and patterns."""
        try:
            # Load spaCy model
            self.nlp = spacy.load("en_core_web_lg")
            logger.info("Loaded spaCy model successfully")
            
            # Initialize sentence transformer
            self.sentence_model = SentenceTransformer('all-MiniLM-L6-v2')
            logger.info("Loaded sentence transformer model successfully")
            
            # Download required NLTK data
            nltk.download('punkt', quiet=True)
            nltk.download('averaged_perceptron_tagger', quiet=True)
            
            # Define patterns for financial documents
            self.financial_patterns = {
                'section_header': r'^[A-Z][A-Z\s]+:$',
                'financial_number': r'\$?\d+(?:,\d{3})*(?:\.\d{2})?',
                'date': r'\d{1,2}/\d{1,2}/\d{2,4}',
                'bullet_point': r'^[\•\-\*]\s',
                'numbered_list': r'^\d+\.\s'
            }
            
            # Compile regex patterns
            self.compiled_patterns = {
                key: re.compile(pattern) 
                for key, pattern in self.financial_patterns.items()
            }
            
            logger.info("SemanticSegmenter initialized successfully")
            
        except Exception as e:
            logger.error(f"Error initializing SemanticSegmenter: {str(e)}")
            raise

    def preprocess_text(self, text: str) -> str:
        """
        Clean and normalize text while preserving important structure.
        
        Args:
            text (str): Input text to preprocess
            
        Returns:
            str: Preprocessed text
        """
        try:
            # Remove extra whitespace while preserving paragraph breaks
            text = re.sub(r'\n\s*\n', '\n\n', text)
            
            # Normalize spaces
            text = re.sub(r' +', ' ', text)
            
            # Preserve financial numbers and dates
            # We'll store them temporarily and restore after cleaning
            number_placeholders = {}
            date_placeholders = {}
            
            # Store financial numbers
            for i, match in enumerate(re.finditer(self.financial_patterns['financial_number'], text)):
                placeholder = f"__NUMBER_{i}__"
                number_placeholders[placeholder] = match.group()
                text = text.replace(match.group(), placeholder)
            
            # Store dates
            for i, match in enumerate(re.finditer(self.financial_patterns['date'], text)):
                placeholder = f"__DATE_{i}__"
                date_placeholders[placeholder] = match.group()
                text = text.replace(match.group(), placeholder)
            
            # Basic cleaning
            text = text.strip()
            
            # Restore placeholders
            for placeholder, value in number_placeholders.items():
                text = text.replace(placeholder, value)
            for placeholder, value in date_placeholders.items():
                text = text.replace(placeholder, value)
            
            return text
            
        except Exception as e:
            logger.error(f"Error in preprocess_text: {str(e)}")
            raise

    def detect_boundaries(self, text: str) -> List[Segment]:
        """
        Identify semantic boundaries in text.
        
        Args:
            text (str): Input text to segment
            
        Returns:
            List[Segment]: List of detected segments
        """
        try:
            segments = []
            current_position = 0
            
            # Process text with spaCy
            doc = self.nlp(text)
            
            # First pass: Identify major structural elements
            for sent in doc.sents:
                sent_text = sent.text.strip()
                if not sent_text:
                    continue
                
                # Check for section headers
                if re.match(self.financial_patterns['section_header'], sent_text):
                    segments.append(Segment(
                        text=sent_text,
                        start_index=sent.start_char,
                        end_index=sent.end_char,
                        segment_type='header'
                    ))
                else:
                    segments.append(Segment(
                        text=sent_text,
                        start_index=sent.start_char,
                        end_index=sent.end_char,
                        segment_type='sentence'
                    ))
            
            return segments
            
        except Exception as e:
            logger.error(f"Error in detect_boundaries: {str(e)}")
            raise

    def group_similar_content(self, segments: List[Segment]) -> List[List[Segment]]:
        """
        Group semantically similar segments.
        
        Args:
            segments (List[Segment]): List of segments to group
            
        Returns:
            List[List[Segment]]: Groups of similar segments
        """
        try:
            if not segments:
                return []
            
            # Convert segments to embeddings
            texts = [segment.text for segment in segments]
            embeddings = self.sentence_model.encode(texts)
            
            # Simple grouping based on similarity threshold
            groups = []
            current_group = [segments[0]]
            
            for i in range(1, len(segments)):
                current_embedding = embeddings[i]
                prev_embedding = embeddings[i-1]
                
                # Calculate cosine similarity
                similarity = self.calculate_similarity(current_embedding, prev_embedding)
                
                if similarity > 0.7:  # Threshold for grouping
                    current_group.append(segments[i])
                else:
                    groups.append(current_group)
                    current_group = [segments[i]]
            
            if current_group:
                groups.append(current_group)
            
            return groups
            
        except Exception as e:
            logger.error(f"Error in group_similar_content: {str(e)}")
            raise

    def calculate_similarity(self, embedding1, embedding2) -> float:
        """
        Calculate cosine similarity between two embeddings.
        
        Args:
            embedding1: First embedding
            embedding2: Second embedding
            
        Returns:
            float: Similarity score between 0 and 1
        """
        try:
            from numpy import dot
            from numpy.linalg import norm
            
            return dot(embedding1, embedding2) / (norm(embedding1) * norm(embedding2))
            
        except Exception as e:
            logger.error(f"Error in calculate_similarity: {str(e)}")
            raise

    def create_semantic_segments(self, text: str) -> List[Dict]:
        """
        Main method to create semantic segments.
        
        Args:
            text (str): Input text to segment
            
        Returns:
            List[Dict]: List of semantic segments with metadata
        """
        try:
            # 1. Preprocess
            cleaned_text = self.preprocess_text(text)
            
            # 2. Detect boundaries
            segments = self.detect_boundaries(cleaned_text)
            
            # 3. Group similar content
            grouped_segments = self.group_similar_content(segments)
            
            # 4. Format and return
            result = []
            for i, group in enumerate(grouped_segments):
                combined_text = ' '.join(segment.text for segment in group)
                result.append({
                    'text': combined_text,
                    'segment_id': i,
                    'start_index': group[0].start_index,
                    'end_index': group[-1].end_index,
                    'segment_type': group[0].segment_type,
                    'created_at': datetime.utcnow().isoformat()
                })
            
            return result
            
        except Exception as e:
            logger.error(f"Error in create_semantic_segments: {str(e)}")
            raise 