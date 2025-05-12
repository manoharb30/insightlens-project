# Use an official Python runtime as a parent image
FROM python:3.11-slim
LABEL authors="Your Name"

# Set the working directory in the container
WORKDIR /app

# Create a non-root user and group
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

# Install system dependencies (if any, uncomment below)
# RUN apt-get update && apt-get install -y --no-install-recommends \
#     build-essential \
#  && rm -rf /var/lib/apt/lists/*

# Copy the dependencies file to the working directory
# Ensure you have a requirements.txt in insightlens-llm-analysis-service-python/
COPY requirements.txt .

# Install any needed packages specified in requirements.txt
RUN pip install --no-cache-dir --trusted-host pypi.python.org --trusted-host files.pythonhosted.org --trusted-host pypi.org -r requirements.txt

# Copy the rest of the application code to the working directory
COPY ./app ./app

# Ensure the app user owns the application files
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER appuser

# Expose the port the app runs on (must match the port in CMD and docker-compose.yml)
EXPOSE 8001

# Command to run the Uvicorn server for FastAPI
# It will look for an 'app' instance in 'app/main.py'
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]