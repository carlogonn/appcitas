from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    # Server
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    DEBUG: bool = False

    # Security
    API_KEY: Optional[str] = None

    # Model paths
    FACE_DETECTION_MODEL: str = "models/det_10g.onnx"
    FACE_RECOGNITION_MODEL: str = "models/w600k_mbf.onnx"

    # Verification thresholds
    MIN_SIMILARITY_SCORE: float = 0.6
    MIN_LIVENESS_SCORE: float = 0.7
    MIN_ANTI_SPOOFING_SCORE: float = 0.7

    # Liveness detection
    LIVENESS_MIN_FRAMES: int = 5
    LIVENESS_MAX_FRAMES: int = 30
    BLINK_THRESHOLD: float = 0.3

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
