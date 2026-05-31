import { Router, Response, NextFunction } from 'express';
import multer from 'multer';
import path from 'path';
import { v4 as uuidv4 } from 'uuid';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';
import env from '../../config/env';

// Configure multer for verification uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/verification/');
  },
  filename: (req, file, cb) => {
    const uniqueName = `${uuidv4()}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

const upload = multer({
  storage,
  limits: {
    fileSize: 50 * 1024 * 1024, // 50MB for videos
  },
  fileFilter: (req, file, cb) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'video/mp4', 'video/quicktime'];
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only JPEG, PNG, WebP, MP4 and MOV are allowed.'));
    }
  },
});

const router = Router();

// Get verification status
router.get('/status', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const verification = await prisma.verification.findFirst({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
    });

    if (!verification) {
      res.json({ status: 'none' });
      return;
    }

    res.json({
      id: verification.id,
      status: verification.status,
      aiScore: verification.aiScore,
      rejectionReason: verification.rejectionReason,
      createdAt: verification.createdAt.toISOString(),
      reviewedAt: verification.reviewedAt?.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

// Submit verification
router.post(
  '/submit',
  authMiddleware,
  upload.fields([
    { name: 'profile_photo', maxCount: 1 },
    { name: 'selfie', maxCount: 1 },
    { name: 'liveness_video', maxCount: 1 },
  ]),
  async (req: AuthRequest, res: Response, next: NextFunction) => {
    try {
      const files = req.files as { [fieldname: string]: Express.Multer.File[] };

      if (!files.profile_photo || !files.selfie) {
        res.status(400).json({ error: 'Profile photo and selfie are required' });
        return;
      }

      const profilePhoto = files.profile_photo[0];
      const selfie = files.selfie[0];
      const livenessVideo = files.liveness_video?.[0];

      // Create verification record
      const verification = await prisma.verification.create({
        data: {
          userId: req.userId!,
          profilePhotoUrl: `/uploads/verification/${profilePhoto.filename}`,
          selfieUrl: `/uploads/verification/${selfie.filename}`,
          livenessVideoUrl: livenessVideo ? `/uploads/verification/${livenessVideo.filename}` : null,
          status: 'pending',
        },
      });

      // TODO: Call AI service for verification
      // For now, simulate AI verification
      const aiResult = {
        status: 'approved',
        face_detected: true,
        similarity_score: 0.85,
        liveness_score: 0.92,
        anti_spoofing_score: 0.88,
      };

      // Update verification with AI result
      await prisma.verification.update({
        where: { id: verification.id },
        data: {
          status: aiResult.status,
          aiScore: aiResult.similarity_score,
          reviewedAt: new Date(),
        },
      });

      // If approved, update user verification status
      if (aiResult.status === 'approved') {
        await prisma.user.update({
          where: { id: req.userId! },
          data: {
            isVerified: true,
            verificationDate: new Date(),
          },
        });
      }

      res.status(201).json({
        id: verification.id,
        status: aiResult.status,
        faceDetected: aiResult.face_detected,
        similarityScore: aiResult.similarity_score,
        livenessScore: aiResult.liveness_score,
        antiSpoofingScore: aiResult.anti_spoofing_score,
        rejectionReason: aiResult.status === 'rejected' ? 'Verificación no aprobada' : null,
      });
    } catch (error) {
      next(error);
    }
  }
);

// Get verification history
router.get('/history', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const verifications = await prisma.verification.findMany({
      where: { userId: req.userId! },
      orderBy: { createdAt: 'desc' },
      take: 10,
    });

    res.json(
      verifications.map((v) => ({
        id: v.id,
        status: v.status,
        createdAt: v.createdAt.toISOString(),
        reviewedAt: v.reviewedAt?.toISOString(),
      }))
    );
  } catch (error) {
    next(error);
  }
});

export default router;
