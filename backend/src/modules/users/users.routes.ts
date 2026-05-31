import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get current user
router.get('/me', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.userId },
      select: {
        id: true,
        email: true,
        username: true,
        phone: true,
        birthDate: true,
        gender: true,
        bio: true,
        profilePhotoUrl: true,
        isVerified: true,
        showDistance: true,
        createdAt: true,
        updatedAt: true,
      },
    });

    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    res.json({
      ...user,
      birthDate: user.birthDate.toISOString(),
      createdAt: user.createdAt.toISOString(),
      updatedAt: user.updatedAt.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

// Get user by ID
router.get('/:id', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.params.id },
      select: {
        id: true,
        email: true,
        username: true,
        phone: true,
        birthDate: true,
        gender: true,
        bio: true,
        profilePhotoUrl: true,
        isVerified: true,
        showDistance: true,
        createdAt: true,
        updatedAt: true,
      },
    });

    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    res.json({
      ...user,
      birthDate: user.birthDate.toISOString(),
      createdAt: user.createdAt.toISOString(),
      updatedAt: user.updatedAt.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

// Update profile
router.patch('/me', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { username, bio, phone, showDistance } = req.body;

    const user = await prisma.user.update({
      where: { id: req.userId },
      data: {
        ...(username && { username }),
        ...(bio !== undefined && { bio }),
        ...(phone !== undefined && { phone }),
        ...(showDistance !== undefined && { showDistance }),
      },
      select: {
        id: true,
        email: true,
        username: true,
        phone: true,
        birthDate: true,
        gender: true,
        bio: true,
        profilePhotoUrl: true,
        isVerified: true,
        showDistance: true,
        createdAt: true,
        updatedAt: true,
      },
    });

    res.json({
      ...user,
      birthDate: user.birthDate.toISOString(),
      createdAt: user.createdAt.toISOString(),
      updatedAt: user.updatedAt.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

export default router;
