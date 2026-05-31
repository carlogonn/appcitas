import numpy as np
from insightface.app import FaceAnalysis
from typing import Optional
import cv2


class FaceRecognitionService:
    def __init__(self):
        """Initialize face recognition using InsightFace ArcFace."""
        self.app = FaceAnalysis(
            name="buffalo_l",
            providers=["CPUExecutionProvider"],
            allowed_modules=["detection", "recognition"],
        )
        self.app.prepare(ctx_id=0, det_size=(640, 640))
        print("Face recognition model loaded successfully")

    def compare_faces(
        self, face1: np.ndarray, face2: np.ndarray
    ) -> float:
        """
        Compare two face embeddings and return similarity score.
        
        Args:
            face1: First face detection (with embedding)
            face2: Second face detection (with embedding)
            
        Returns:
            Similarity score between 0 and 1
        """
        try:
            # Get embeddings
            embedding1 = face1.normed_embedding
            embedding2 = face2.normed_embedding

            # Calculate cosine similarity
            similarity = np.dot(embedding1, embedding2)

            # Ensure score is between 0 and 1
            similarity = max(0.0, min(1.0, float(similarity)))

            return similarity

        except Exception as e:
            print(f"Error comparing faces: {e}")
            return 0.0

    def get_embedding(self, image_data: bytes) -> Optional[np.ndarray]:
        """
        Extract face embedding from image.
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Face embedding vector or None
        """
        try:
            nparr = np.frombuffer(image_data, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if img is None:
                return None

            faces = self.app.get(img)

            if not faces:
                return None

            # Return embedding of largest face
            largest_face = max(faces, key=lambda f: f.bbox[2] * f.bbox[3])
            return largest_face.normed_embedding

        except Exception as e:
            print(f"Error getting embedding: {e}")
            return None

    def find_matching_faces(
        self,
        query_embedding: np.ndarray,
        database_embeddings: list,
        threshold: float = 0.6,
    ) -> list:
        """
        Find matching faces in database.
        
        Args:
            query_embedding: Query face embedding
            database_embeddings: List of (id, embedding) tuples
            threshold: Minimum similarity threshold
            
        Returns:
            List of (id, score) tuples for matching faces
        """
        matches = []

        for user_id, db_embedding in database_embeddings:
            similarity = float(np.dot(query_embedding, db_embedding))
            if similarity >= threshold:
                matches.append((user_id, similarity))

        # Sort by similarity descending
        matches.sort(key=lambda x: x[1], reverse=True)

        return matches
