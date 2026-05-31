import { Router, Response, NextFunction } from 'express';
import multer from 'multer';
import { v4 as uuidv4 } from 'uuid';
import path from 'path';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    const uniqueName = `${uuidv4()}${path.extname(file.originalname)}`;
    cb(null, uniqueName);
  },
});

const upload = multer({
  storage,
  limits: {
    fileSize: 10 * 1024 * 1024, // 10MB
  },
  fileFilter: (req, file, cb) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (allowedTypes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type. Only JPEG, PNG and WebP are allowed.'));
    }
  },
});

const router = Router();

// Upload photo
router.post(
  '/photos',
  authMiddleware,
  upload.single('photo'),
  async (req: AuthRequest, res: Response, next: NextFunction) => {
    try {
      if (!req.file) {
        res.status(400).json({ error: 'No file uploaded' });
        return;
      }

      const isPrimary = req.body.is_primary === 'true';
      const photoUrl = `/uploads/${req.file.filename}`;

      // Get current photo count
      const photoCount = await prisma.userPhoto.count({
        where: { userId: req.userId! },
      });

      // If this is the first photo or isPrimary is true, set as primary
      const shouldBePrimary = photoCount === 0 || isPrimary;

      // If setting as primary, unset other primary photos
      if (shouldBePrimary) {
        await prisma.userPhoto.updateMany({
          where: { userId: req.userId!, isPrimary: true },
          data: { isPrimary: false },
        });
      }

      const photo = await prisma.userPhoto.create({
        data: {
          userId: req.userId!,
          photoUrl,
          isPrimary: shouldBePrimary,
          orderIndex: photoCount,
        },
      });

      // Update user profile photo if this is primary
      if (shouldBePrimary) {
        await prisma.user.update({
          where: { id: req.userId! },
          data: { profilePhotoUrl: photoUrl },
        });
      }

      res.status(201).json({
        id: photo.id,
        photoUrl: photo.photoUrl,
        isPrimary: photo.isPrimary,
        orderIndex: photo.orderIndex,
      });
    } catch (error) {
      next(error);
    }
  }
);

// Get user photos
router.get('/photos', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const photos = await prisma.userPhoto.findMany({
      where: { userId: req.userId! },
      orderBy: { orderIndex: 'asc' },
    });

    res.json(
      photos.map((photo) => ({
        id: photo.id,
        photoUrl: photo.photoUrl,
        isPrimary: photo.isPrimary,
        orderIndex: photo.orderIndex,
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Delete photo
router.delete('/photos/:photoId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const photoId = req.params.photoId as string;

    const photo = await prisma.userPhoto.findFirst({
      where: { id: photoId, userId: req.userId! },
    });

    if (!photo) {
      res.status(404).json({ error: 'Photo not found' });
      return;
    }

    // Delete the photo
    await prisma.userPhoto.delete({
      where: { id: photoId },
    });

    // If deleted photo was primary, set another as primary
    if (photo.isPrimary) {
      const nextPrimary = await prisma.userPhoto.findFirst({
        where: { userId: req.userId! },
        orderBy: { orderIndex: 'asc' },
      });

      if (nextPrimary) {
        await prisma.userPhoto.update({
          where: { id: nextPrimary.id },
          data: { isPrimary: true },
        });

        await prisma.user.update({
          where: { id: req.userId! },
          data: { profilePhotoUrl: nextPrimary.photoUrl },
        });
      } else {
        // No photos left, clear profile photo
        await prisma.user.update({
          where: { id: req.userId! },
          data: { profilePhotoUrl: null },
        });
      }
    }

    // Delete file from disk
    const fs = require('fs');
    const filePath = path.join(__dirname, '../../..', photo.photoUrl);
    if (fs.existsSync(filePath)) {
      fs.unlinkSync(filePath);
    }

    res.json({ message: 'Photo deleted successfully' });
  } catch (error) {
    next(error);
  }
});

// Reorder photos
router.patch('/photos/reorder', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { photoIds } = req.body;

    if (!Array.isArray(photoIds)) {
      res.status(400).json({ error: 'photoIds must be an array' });
      return;
    }

    // Update order for each photo
    for (let i = 0; i < photoIds.length; i++) {
      await prisma.userPhoto.update({
        where: { id: photoIds[i], userId: req.userId! },
        data: { orderIndex: i },
      });
    }

    res.json({ message: 'Photos reordered successfully' });
  } catch (error) {
    next(error);
  }
});

// Set primary photo
router.patch('/photos/:photoId/primary', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const photoId = req.params.photoId as string;

    const photo = await prisma.userPhoto.findFirst({
      where: { id: photoId, userId: req.userId! },
    });

    if (!photo) {
      res.status(404).json({ error: 'Photo not found' });
      return;
    }

    // Unset current primary
    await prisma.userPhoto.updateMany({
      where: { userId: req.userId!, isPrimary: true },
      data: { isPrimary: false },
    });

    // Set new primary
    await prisma.userPhoto.update({
      where: { id: photoId },
      data: { isPrimary: true },
    });

    // Update user profile photo
    await prisma.user.update({
      where: { id: req.userId! },
      data: { profilePhotoUrl: photo.photoUrl },
    });

    res.json({ message: 'Primary photo updated successfully' });
  } catch (error) {
    next(error);
  }
});

export default router;
