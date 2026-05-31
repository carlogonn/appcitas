import cv2
import numpy as np
from insightface.app import FaceAnalysis
from typing import Optional, List
import io
from PIL import Image


class FaceDetectionService:
    def __init__(self):
        """Initialize face detection using InsightFace SCRFD."""
        self.app = FaceAnalysis(
            name="buffalo_l",
            providers=["CPUExecutionProvider"],
            allowed_modules=["detection"],
        )
        self.app.prepare(ctx_id=0, det_size=(640, 640))
        print("Face detection model loaded successfully")

    def detect_face(self, image_data: bytes) -> Optional[np.ndarray]:
        """
        Detect face in image and return face embedding.
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Face embedding or None if no face detected
        """
        try:
            # Convert bytes to numpy array
            nparr = np.frombuffer(image_data, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if img is None:
                return None

            # Detect faces
            faces = self.app.get(img)

            if not faces:
                return None

            # Return the largest face (most prominent)
            largest_face = max(faces, key=lambda f: f.bbox[2] * f.bbox[3])
            return largest_face

        except Exception as e:
            print(f"Error in face detection: {e}")
            return None

    def detect_faces(self, image_data: bytes) -> List:
        """
        Detect all faces in image.
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            List of face detections
        """
        try:
            nparr = np.frombuffer(image_data, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if img is None:
                return []

            faces = self.app.get(img)
            return faces

        except Exception as e:
            print(f"Error in face detection: {e}")
            return []

    def get_face_bbox(self, image_data: bytes) -> Optional[List[int]]:
        """
        Get bounding box of the main face.
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Bounding box [x1, y1, x2, y2] or None
        """
        face = self.detect_face(image_data)
        if face is not None:
            return face.bbox.tolist()
        return None
