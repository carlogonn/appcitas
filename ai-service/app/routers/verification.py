from fastapi import APIRouter, Request, HTTPException, UploadFile, File
from pydantic import BaseModel
from typing import Optional
import base64
import io

from app.config import settings

router = APIRouter()


class VerificationRequest(BaseModel):
    user_id: str
    profile_photo: str  # base64 encoded
    selfie: str  # base64 encoded
    liveness_video: Optional[str] = None  # base64 encoded


class VerificationResponse(BaseModel):
    status: str  # approved, rejected
    face_detected: bool
    similarity_score: float
    liveness_score: float
    anti_spoofing_score: float
    rejection_reason: Optional[str] = None


@router.post("/verify", response_model=VerificationResponse)
async def verify_user(request: Request, verification: VerificationRequest):
    """
    Verify user identity using face detection and recognition.
    
    1. Detect face in profile photo and selfie
    2. Compare faces for similarity
    3. Perform liveness detection on video
    4. Return verification result
    """
    try:
        # Decode images from base64
        profile_photo_data = base64.b64decode(verification.profile_photo)
        selfie_data = base64.b64decode(verification.selfie)

        # Get services from app state
        face_detection = request.app.state.face_detection
        face_recognition = request.app.state.face_recognition
        liveness = request.app.state.liveness

        # 1. Detect faces
        profile_face = face_detection.detect_face(profile_photo_data)
        selfie_face = face_detection.detect_face(selfie_data)

        if profile_face is None or selfie_face is None:
            return VerificationResponse(
                status="rejected",
                face_detected=False,
                similarity_score=0.0,
                liveness_score=0.0,
                anti_spoofing_score=0.0,
                rejection_reason="No se detectó rostro en una o ambas imágenes"
            )

        # 2. Compare faces (similarity score)
        similarity_score = face_recognition.compare_faces(profile_face, selfie_face)

        # 3. Liveness detection
        liveness_score = 0.0
        if verification.liveness_video:
            liveness_result = liveness.detect_liveness(verification.liveness_video)
            liveness_score = liveness_result["score"]

        # 4. Anti-spoofing check
        anti_spoofing_score = liveness.check_anti_spoofing(selfie_data)

        # 5. Determine result
        face_detected = True
        approved = (
            similarity_score >= settings.MIN_SIMILARITY_SCORE and
            liveness_score >= settings.MIN_LIVENESS_SCORE and
            anti_spoofing_score >= settings.MIN_ANTI_SPOOFING_SCORE
        )

        status = "approved" if approved else "rejected"
        rejection_reason = None

        if not approved:
            reasons = []
            if similarity_score < settings.MIN_SIMILARITY_SCORE:
                reasons.append("Las fotos no coinciden lo suficiente")
            if liveness_score < settings.MIN_LIVENESS_SCORE:
                reasons.append("No se detectó actividad vital")
            if anti_spoofing_score < settings.MIN_ANTI_SPOOFING_SCORE:
                reasons.append("Se detectó posible intento de suplantación")
            rejection_reason = ". ".join(reasons)

        return VerificationResponse(
            status=status,
            face_detected=face_detected,
            similarity_score=round(similarity_score, 4),
            liveness_score=round(liveness_score, 4),
            anti_spoofing_score=round(anti_spoofing_score, 4),
            rejection_reason=rejection_reason
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error en verificación: {str(e)}")


@router.post("/verify/upload", response_model=VerificationResponse)
async def verify_user_upload(
    request: Request,
    user_id: str,
    profile_photo: UploadFile = File(...),
    selfie: UploadFile = File(...),
    liveness_video: Optional[UploadFile] = File(None)
):
    """
    Verify user identity using file uploads instead of base64.
    """
    try:
        # Read file contents
        profile_photo_data = await profile_photo.read()
        selfie_data = await selfie.read()
        liveness_video_data = await liveness_video.read() if liveness_video else None

        # Get services from app state
        face_detection = request.app.state.face_detection
        face_recognition = request.app.state.face_recognition
        liveness = request.app.state.liveness

        # 1. Detect faces
        profile_face = face_detection.detect_face(profile_photo_data)
        selfie_face = face_detection.detect_face(selfie_data)

        if profile_face is None or selfie_face is None:
            return VerificationResponse(
                status="rejected",
                face_detected=False,
                similarity_score=0.0,
                liveness_score=0.0,
                anti_spoofing_score=0.0,
                rejection_reason="No se detectó rostro en una o ambas imágenes"
            )

        # 2. Compare faces
        similarity_score = face_recognition.compare_faces(profile_face, selfie_face)

        # 3. Liveness detection
        liveness_score = 0.0
        if liveness_video_data:
            liveness_result = liveness.detect_liveness_video(liveness_video_data)
            liveness_score = liveness_result["score"]

        # 4. Anti-spoofing check
        anti_spoofing_score = liveness.check_anti_spoofing(selfie_data)

        # 5. Determine result
        face_detected = True
        approved = (
            similarity_score >= settings.MIN_SIMILARITY_SCORE and
            liveness_score >= settings.MIN_LIVENESS_SCORE and
            anti_spoofing_score >= settings.MIN_ANTI_SPOOFING_SCORE
        )

        status = "approved" if approved else "rejected"
        rejection_reason = None

        if not approved:
            reasons = []
            if similarity_score < settings.MIN_SIMILARITY_SCORE:
                reasons.append("Las fotos no coinciden lo suficiente")
            if liveness_score < settings.MIN_LIVENESS_SCORE:
                reasons.append("No se detectó actividad vital")
            if anti_spoofing_score < settings.MIN_ANTI_SPOOFING_SCORE:
                reasons.append("Se detectó posible intento de suplantación")
            rejection_reason = ". ".join(reasons)

        return VerificationResponse(
            status=status,
            face_detected=face_detected,
            similarity_score=round(similarity_score, 4),
            liveness_score=round(liveness_score, 4),
            anti_spoofing_score=round(anti_spoofing_score, 4),
            rejection_reason=rejection_reason
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error en verificación: {str(e)}")
