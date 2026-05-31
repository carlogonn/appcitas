from fastapi import FastAPI, HTTPException, Depends, Security
from fastapi.security import APIKeyHeader
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import uvicorn

from app.config import settings
from app.routers import verification, health
from app.services.face_detection import FaceDetectionService
from app.services.face_recognition import FaceRecognitionService
from app.services.liveness import LivenessService


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Load AI models
    print("Loading AI models...")
    app.state.face_detection = FaceDetectionService()
    app.state.face_recognition = FaceRecognitionService()
    app.state.liveness = LivenessService()
    print("AI models loaded successfully!")
    yield
    # Shutdown: Cleanup
    print("Shutting down...")


app = FastAPI(
    title="AppCitas AI Verification Service",
    description="Face verification and liveness detection API",
    version="1.0.0",
    lifespan=lifespan,
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API Key authentication
api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)


async def verify_api_key(api_key: str = Security(api_key_header)):
    if settings.API_KEY and api_key != settings.API_KEY:
        raise HTTPException(status_code=403, detail="Invalid API key")
    return api_key


# Include routers
app.include_router(health.router, tags=["Health"])
app.include_router(
    verification.router,
    prefix="/api/v1",
    tags=["Verification"],
    dependencies=[Depends(verify_api_key)],
)


@app.get("/")
async def root():
    return {
        "service": "AppCitas AI Verification",
        "version": "1.0.0",
        "status": "running",
    }


if __name__ == "__main__":
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=settings.DEBUG)
