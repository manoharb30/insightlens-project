from typing import List
from fastapi import FastAPI
from pydantic import BaseModel

# Define request and response models
class EmbedRequest(BaseModel):
    texts: List[str]

class EmbedResponse(BaseModel):
    embeddings: List[List[float]]

# Create FastAPI app instance
app = FastAPI(
    title="InsightLens Embedding Service",
    description="Service for generating text embeddings",
    version="0.1.0"
)

@app.get("/health")
async def health_check() -> dict:
    """Health check endpoint."""
    return {"status": "ok"}

@app.post("/embed", response_model=EmbedResponse)
async def generate_embeddings(request: EmbedRequest) -> EmbedResponse:
    """
    Generate embeddings for the provided texts.
    Currently returns dummy embeddings of dimension 384 (matching BAAI/bge-small-en-v1.5).
    """
    # Log the received texts
    print(f"Received {len(request.texts)} texts for embedding")
    
    # Generate dummy embeddings (384-dimensional vectors)
    dummy_embeddings = [[0.0] * 384 for _ in request.texts]
    
    return EmbedResponse(embeddings=dummy_embeddings) 