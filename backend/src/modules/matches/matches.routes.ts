import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get all matches for current user
router.get('/', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const userId = req.userId!;

    const matches = await prisma.match.findMany({
      where: {
        OR: [
          { user1Id: userId },
          { user2Id: userId },
        ],
      },
      include: {
        user1: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
            isVerified: true,
          },
        },
        user2: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
            isVerified: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    });

    res.json(
      matches.map((match) => {
        const otherUser = match.user1Id === userId ? match.user2 : match.user1;
        return {
          id: match.id,
          user1Id: match.user1Id,
          user2Id: match.user2Id,
          user: {
            id: otherUser.id,
            username: otherUser.username,
            profilePhotoUrl: otherUser.profilePhotoUrl,
            isVerified: otherUser.isVerified,
          },
          createdAt: match.createdAt.toISOString(),
        };
      })
    );
  } catch (error) {
    next(error);
  }
});

// Get a specific match
router.get('/:matchId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { matchId } = req.params;
    const userId = req.userId!;

    const match = await prisma.match.findUnique({
      where: { id: matchId },
      include: {
        user1: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
            isVerified: true,
          },
        },
        user2: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
            isVerified: true,
          },
        },
      },
    });

    if (!match) {
      res.status(404).json({ error: 'Match not found' });
      return;
    }

    // Check if user is part of this match
    if (match.user1Id !== userId && match.user2Id !== userId) {
      res.status(403).json({ error: 'Access denied' });
      return;
    }

    const otherUser = match.user1Id === userId ? match.user2 : match.user1;

    res.json({
      id: match.id,
      user1Id: match.user1Id,
      user2Id: match.user2Id,
      user: {
        id: otherUser.id,
        username: otherUser.username,
        profilePhotoUrl: otherUser.profilePhotoUrl,
        isVerified: otherUser.isVerified,
      },
      createdAt: match.createdAt.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

// Swipe on a user
router.post('/swipe', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { targetUserId, isLike } = req.body;
    const userId = req.userId!;

    // Check if target user exists
    const targetUser = await prisma.user.findUnique({
      where: { id: targetUserId },
    });

    if (!targetUser) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    // Check if already swiped
    const existingSwipe = await prisma.swipe.findUnique({
      where: {
        swiperId_swipedId: {
          swiperId: userId,
          swipedId: targetUserId,
        },
      },
    });

    if (existingSwipe) {
      res.status(400).json({ error: 'Already swiped on this user' });
      return;
    }

    // Create swipe
    await prisma.swipe.create({
      data: {
        swiperId: userId,
        swipedId: targetUserId,
        isLike,
      },
    });

    // Check if it's a match (target user liked us before)
    let isMatch = false;
    let matchId = null;

    if (isLike) {
      const reverseSwipe = await prisma.swipe.findUnique({
        where: {
          swiperId_swipedId: {
            swiperId: targetUserId,
            swipedId: userId,
          },
        },
      });

      if (reverseSwipe && reverseSwipe.isLike) {
        // It's a match!
        isMatch = true;

        // Create match record
        const match = await prisma.match.create({
          data: {
            user1Id: userId,
            user2Id: targetUserId,
          },
        });

        matchId = match.id;
      }
    }

    res.json({
      isMatch,
      matchId,
    });
  } catch (error) {
    next(error);
  }
});

// Get discover users (users to swipe on)
router.get('/discover', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const userId = req.userId!;
    const { page = '1', limit = '20' } = req.query;
    const skip = (parseInt(page as string, 10) - 1) * parseInt(limit as string, 10);

    // Get users that:
    // 1. Are not the current user
    // 2. Haven't been swiped by the current user
    // 3. Are not blocked by the current user
    // 4. Are not blocking the current user

    const swipedUserIds = await prisma.swipe.findMany({
      where: { swiperId: userId },
      select: { swipedId: true },
    });

    const blockedUserIds = await prisma.block.findMany({
      where: {
        OR: [
          { blockerId: userId },
          { blockedId: userId },
        ],
      },
      select: {
        blockerId: true,
        blockedId: true,
      },
    });

    const excludedIds = [
      userId,
      ...swipedUserIds.map((s) => s.swipedId),
      ...blockedUserIds.map((b) => b.blockerId === userId ? b.blockedId : b.blockerId),
    ];

    const users = await prisma.user.findMany({
      where: {
        id: { notIn: excludedIds },
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
      skip,
      take: parseInt(limit as string, 10),
      orderBy: { createdAt: 'desc' },
    });

    res.json(
      users.map((user) => ({
        id: user.id,
        email: user.email,
        username: user.username,
        phone: user.phone,
        birthDate: user.birthDate.toISOString(),
        gender: user.gender,
        bio: user.bio,
        profilePhotoUrl: user.profilePhotoUrl,
        isVerified: user.isVerified,
        showDistance: user.showDistance,
        createdAt: user.createdAt.toISOString(),
        updatedAt: user.updatedAt.toISOString(),
      }))
    );
  } catch (error) {
    next(error);
  }
});

export default router;
