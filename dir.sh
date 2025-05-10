#!/bin/bash

# Ensure we are in the intended root folder (optional check, good practice)
# CURRENT_DIR_NAME=${PWD##*/} # Gets the current directory name
# if [ "$CURRENT_DIR_NAME" != "insightlens-project" ]; then
#   echo "Please run this script from within the 'insightlens-project' root directory."
#   exit 1
# fi

echo "Creating service directories for InsightLens project..."

# 1. Java Backend Service
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/config
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/controller
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/model
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/repository
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/service
mkdir -p insightlens-backend-java/src/main/java/com/insightlens/core/util # For general utils if needed
mkdir -p insightlens-backend-java/src/main/resources
mkdir -p insightlens-backend-java/src/test/java/com/insightlens/core

echo "Created directories for insightlens-backend-java"

# 2. Python Embedding Service
mkdir -p insightlens-embedding-service-python/app/core
mkdir -p insightlens-embedding-service-python/app/services
mkdir -p insightlens-embedding-service-python/tests

echo "Created directories for insightlens-embedding-service-python"

# 3. Python LLM Analysis Service
mkdir -p insightlens-llm-analysis-service-python/app/core
mkdir -p insightlens-llm-analysis-service-python/app/services
mkdir -p insightlens-llm-analysis-service-python/app/prompts
mkdir -p insightlens-llm-analysis-service-python/tests

echo "Created directories for insightlens-llm-analysis-service-python"

# 4. React Frontend Application
mkdir -p insightlens-frontend-react/public
mkdir -p insightlens-frontend-react/src/components
mkdir -p insightlens-frontend-react/src/pages
mkdir -p insightlens-frontend-react/src/services
mkdir -p insightlens-frontend-react/src/contexts
mkdir -p insightlens-frontend-react/src/hooks
mkdir -p insightlens-frontend-react/src/assets # For images, fonts etc.

echo "Created directories for insightlens-frontend-react"

# 5. Shared top-level directories
mkdir -p scripts
mkdir -p docs

echo "Created shared directories (scripts, docs)"
echo "---"
echo "Directory structure created successfully within /Users/shreshta/insightlens-project/"
echo "You can now proceed with Cursor AI prompts to generate file content within these directories."