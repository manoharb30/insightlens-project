# Text Segmentation Service

A FastAPI microservice that implements semantic text segmentation using a multi-stage approach.

## Features

- Heading-based text splitting
- TextTiling for sub-topic detection
- Optional embedding-based refinement using sentence-transformers
- Production-ready error handling and logging

## Setup

1. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Download required NLTK data:
```python
python -c "import nltk; nltk.download('punkt')"
```

4. Download spaCy model:
```bash
python -m spacy download en_core_web_sm
```

## Running the Service

Start the service with:
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

The service will be available at `http://localhost:8000`

## API Documentation

Once the service is running, visit:
- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

## API Usage

Send a POST request to `/segment` with the following JSON payload:

```json
{
    "documentId": "your-document-id",
    "text": "Your text content to segment..."
}
```

The response will be a list of segments:

```json
{
    "segments": [
        {
            "segmentOrder": 0,
            "text": "First semantic chunk..."
        },
        {
            "segmentOrder": 1,
            "text": "Second semantic chunk..."
        }
    ]
}
```

## Development

The project structure:
```
insightlens-segmentation-service-python/
├── app/
│   ├── main.py           # FastAPI app and endpoint definitions
│   ├── schemas.py        # Pydantic models for request/response
│   └── segmentation.py   # Core segmentation logic
├── requirements.txt      # Project dependencies
└── README.md            # This file
```

## Future Improvements

- Add LLM-based boundary refinement
- Implement caching for frequently processed documents
- Add metrics collection and monitoring
- Support for different languages 