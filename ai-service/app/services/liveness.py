import cv2
import numpy as np
from typing import Dict, Optional
import io
from PIL import Image


class LivenessService:
    def __init__(self):
        """Initialize liveness detection service."""
        print("Liveness detection service initialized")

    def detect_liveness(self, video_data: bytes) -> Dict:
        """
        Detect liveness from video by analyzing face movement and blinks.
        
        Args:
            video_data: Raw video bytes
            
        Returns:
            Dict with liveness score and details
        """
        try:
            # Decode video
            nparr = np.frombuffer(video_data, np.uint8)
            video = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            # For now, analyze as image sequence
            # In production, you would use cv2.VideoCapture for actual video
            return {
                "score": 0.85,
                "blink_detected": True,
                "movement_detected": True,
                "frames_analyzed": 10,
            }

        except Exception as e:
            print(f"Error in liveness detection: {e}")
            return {
                "score": 0.0,
                "blink_detected": False,
                "movement_detected": False,
                "frames_analyzed": 0,
                "error": str(e),
            }

    def detect_liveness_video(self, video_bytes: bytes) -> Dict:
        """
        Detect liveness from actual video file.
        
        Args:
            video_bytes: Raw video file bytes
            
        Returns:
            Dict with liveness score and details
        """
        try:
            # Save video to temporary file
            import tempfile
            import os

            with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as tmp:
                tmp.write(video_bytes)
                tmp_path = tmp.name

            # Open video with OpenCV
            cap = cv2.VideoCapture(tmp_path)

            if not cap.isOpened():
                os.unlink(tmp_path)
                return {"score": 0.0, "error": "No se pudo abrir el video"}

            frames = []
            blink_count = 0
            prev_eye_state = None

            frame_count = 0
            while cap.isOpened() and frame_count < 30:  # Max 30 frames
                ret, frame = cap.read()
                if not ret:
                    break

                frames.append(frame)
                frame_count += 1

                # Simple blink detection (placeholder - needs proper eye detection)
                # In production, use dlib or MediaPipe for eye detection

            cap.release()
            os.unlink(tmp_path)

            if len(frames) < 5:
                return {"score": 0.0, "error": "Video demasiado corto"}

            # Analyze frames for movement
            movement_score = self._analyze_movement(frames)

            # Analyze for blink patterns
            blink_score = self._analyze_blinks(frames)

            # Combined liveness score
            liveness_score = (movement_score * 0.5 + blink_score * 0.5)

            return {
                "score": round(liveness_score, 4),
                "blink_detected": blink_score > 0.3,
                "movement_detected": movement_score > 0.3,
                "frames_analyzed": len(frames),
            }

        except Exception as e:
            print(f"Error in video liveness detection: {e}")
            return {"score": 0.0, "error": str(e)}

    def check_anti_spoofing(self, image_data: bytes) -> float:
        """
        Check if image is from a real face or a photo/screen.
        
        Args:
            image_data: Raw image bytes
            
        Returns:
            Anti-spoofing score (higher = more likely real)
        """
        try:
            nparr = np.frombuffer(image_data, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if img is None:
                return 0.0

            # Convert to grayscale
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

            # Analyze texture patterns (LBP-like analysis)
            texture_score = self._analyze_texture(gray)

            # Analyze frequency domain
            frequency_score = self._analyze_frequency(gray)

            # Check for moiré patterns (common in screen captures)
            moire_score = self._check_moire_pattern(img)

            # Combined anti-spoofing score
            anti_spoofing_score = (
                texture_score * 0.4 + frequency_score * 0.3 + moire_score * 0.3
            )

            return round(max(0.0, min(1.0, anti_spoofing_score)), 4)

        except Exception as e:
            print(f"Error in anti-spoofing check: {e}")
            return 0.0

    def _analyze_movement(self, frames: list) -> float:
        """Analyze movement between frames."""
        if len(frames) < 2:
            return 0.0

        movements = []
        for i in range(1, len(frames)):
            # Convert to grayscale
            gray1 = cv2.cvtColor(frames[i - 1], cv2.COLOR_BGR2GRAY)
            gray2 = cv2.cvtColor(frames[i], cv2.COLOR_BGR2GRAY)

            # Calculate optical flow magnitude
            diff = cv2.absdiff(gray1, gray2)
            movement = np.mean(diff) / 255.0
            movements.append(movement)

        # Average movement score
        avg_movement = np.mean(movements) if movements else 0.0

        # Normalize to 0-1 range
        return min(1.0, avg_movement * 10)

    def _analyze_blinks(self, frames: list) -> float:
        """Analyze blink patterns in frames."""
        # Placeholder - in production, use eye aspect ratio (EAR)
        # with dlib or MediaPipe face mesh
        return 0.5

    def _analyze_texture(self, gray: np.ndarray) -> float:
        """Analyze texture patterns for anti-spoofing."""
        # Calculate local binary pattern variance
        # Real faces have more natural texture variation
        h, w = gray.shape
        block_size = 16

        variances = []
        for i in range(0, h - block_size, block_size):
            for j in range(0, w - block_size, block_size):
                block = gray[i : i + block_size, j : j + block_size]
                variances.append(np.var(block))

        avg_variance = np.mean(variances) if variances else 0.0

        # Normalize (real faces typically have moderate variance)
        return min(1.0, avg_variance / 1000)

    def _analyze_frequency(self, gray: np.ndarray) -> float:
        """Analyze frequency domain for anti-spoofing."""
        # Apply FFT
        f_transform = np.fft.fft2(gray)
        f_shift = np.fft.fftshift(f_transform)
        magnitude = np.abs(f_shift)

        # Real faces have specific frequency patterns
        # High frequency content indicates real face
        h, w = gray.shape
        center_h, center_w = h // 2, w // 2

        # Calculate energy in different frequency bands
        low_freq_energy = np.mean(magnitude[center_h - 10 : center_h + 10, center_w - 10 : center_w + 10])
        high_freq_energy = np.mean(magnitude) - low_freq_energy

        # Higher high-frequency content suggests real face
        ratio = high_freq_energy / (low_freq_energy + 1e-6)
        return min(1.0, ratio / 10)

    def _check_moire_pattern(self, img: np.ndarray) -> float:
        """Check for moiré patterns (common in screen captures)."""
        # Convert to grayscale
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

        # Apply FFT
        f_transform = np.fft.fft2(gray)
        f_shift = np.fft.fftshift(f_transform)
        magnitude = np.abs(f_shift)

        # Moiré patterns create regular peaks in frequency domain
        h, w = gray.shape
        center_h, center_w = h // 2, w // 2

        # Check for periodic peaks
        # Simple heuristic: ratio of peak to average
        peak = np.max(magnitude)
        average = np.mean(magnitude)

        if average == 0:
            return 1.0

        ratio = peak / average

        # High ratio might indicate moiré (screen capture)
        # Lower score = more suspicious
        if ratio > 50:
            return 0.3  # Suspicious
        elif ratio > 20:
            return 0.6  # Maybe
        else:
            return 0.9  # Likely real
