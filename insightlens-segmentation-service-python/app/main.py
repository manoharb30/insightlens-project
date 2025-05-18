from fastapi import FastAPI, HTTPException
from loguru import logger
import uvicorn

from .schemas import SegmentationRequest, SegmentationResponse, TextSegment
from .segmentation import TextSegmenter

app = FastAPI(
    title="Text Segmentation Service",
    description="A microservice for semantic text segmentation",
    version="1.0.0"
)

# Initialize the segmenter
segmenter = TextSegmenter(use_embeddings=True)

@app.post("/segment", response_model=SegmentationResponse)
async def segment_text(request: SegmentationRequest):
    """
    Segment text into semantic chunks using a multi-stage approach:
    1. Heading-based splitting
    2. TextTiling for sub-topic detection
    3. Embedding-based refinement
    """
    try:
        logger.info(f"Processing document {request.documentId}")
        
        # Perform segmentation
        segments = segmenter.segment_text(request.text)
        
        # Convert to response format
        response_segments = [
            TextSegment(segmentOrder=order, text=text)
            for order, text in segments
        ]
        
        logger.info(f"Successfully segmented document into {len(response_segments)} segments")
        return SegmentationResponse(segments=response_segments)
        
    except Exception as e:
        logger.error(f"Error processing document {request.documentId}: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to process document: {str(e)}"
        )

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    ) 