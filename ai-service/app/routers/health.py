from fastapi import APIRouter, Request
from datetime import datetime

router = APIRouter()


@router.get("/health")
async def health_check(request: Request):
    return {
        "status": "healthy",
        "timestamp": datetime.utcnow().isoformat(),
        "models_loaded": {
            "face_detection": hasattr(request.app.state, "face_detection"),
            "face_recognition": hasattr(request.app.state, "face_recognition"),
            "liveness": hasattr(request.app.state, "liveness"),
        },
    }
