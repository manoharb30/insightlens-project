from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict
from .semantic_segmenter import SemanticSegmenter
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

router = APIRouter()
segmenter = SemanticSegmenter()

class SegmentationRequest(BaseModel):
    documentId: str
    text: str

class SegmentationResponse(BaseModel):
    documentId: str
    segments: List[Dict]

@router.post("/segment", response_model=SegmentationResponse)
async def segment_document(request: SegmentationRequest):
    """
    Segment a document into semantically meaningful chunks.
    
    Args:
        request (SegmentationRequest): The request containing document ID and text
        
    Returns:
        SegmentationResponse: The segmented document
    """
    try:
        logger.info(f"Processing document {request.documentId}")
        
        # Create semantic segments
        segments = segmenter.create_semantic_segments(request.text)
        
        logger.info(f"Created {len(segments)} segments for document {request.documentId}")
        
        return SegmentationResponse(
            documentId=request.documentId,
            segments=segments
        )
        
    except Exception as e:
        logger.error(f"Error processing document {request.documentId}: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Error processing document: {str(e)}"
        ) 