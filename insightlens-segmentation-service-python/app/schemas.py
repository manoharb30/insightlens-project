from pydantic import BaseModel, Field
from typing import List

class SegmentationRequest(BaseModel):
    documentId: str = Field(..., description="Unique identifier for the document")
    text: str = Field(..., description="Text content to be segmented")

class TextSegment(BaseModel):
    segmentOrder: int = Field(..., description="Order of the segment in the document")
    text: str = Field(..., description="Content of the segment")

class SegmentationResponse(BaseModel):
    segments: List[TextSegment] = Field(..., description="List of text segments") 