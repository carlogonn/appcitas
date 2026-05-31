import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Block a user
router.post('/block/:userId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { userId } = req.params;
    const blockerId = req.userId!;

    // Check if trying to block self
    if (userId === blockerId) {
      res.status(400).json({ error: 'Cannot block yourself' });
      return;
    }

    // Check if user exists
    const userToBlock = await prisma.user.findUnique({
      where: { id: userId },
    });

    if (!userToBlock) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    // Check if already blocked
    const existingBlock = await prisma.block.findUnique({
      where: {
        blockerId_blockedId: {
          blockerId,
          blockedId: userId,
        },
      },
    });

    if (existingBlock) {
      res.status(400).json({ error: 'User already blocked' });
      return;
    }

    // Create block
    await prisma.block.create({
      data: {
        blockerId,
        blockedId: userId,
      },
    });

    // Delete any existing match between users
    await prisma.match.deleteMany({
      where: {
        OR: [
          { user1Id: blockerId, user2Id: userId },
          { user1Id: userId, user2Id: blockerId },
        ],
      },
    });

    // Delete any existing swipes
    await prisma.swipe.deleteMany({
      where: {
        OR: [
          { swiperId: blockerId, swipedId: userId },
          { swiperId: userId, swipedId: blockerId },
        ],
      },
    });

    res.json({ message: 'User blocked successfully' });
  } catch (error) {
    next(error);
  }
});

// Unblock a user
router.delete('/block/:userId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { userId } = req.params;
    const blockerId = req.userId!;

    const block = await prisma.block.findUnique({
      where: {
        blockerId_blockedId: {
          blockerId,
          blockedId: userId,
        },
      },
    });

    if (!block) {
      res.status(404).json({ error: 'Block not found' });
      return;
    }

    await prisma.block.delete({
      where: {
        blockerId_blockedId: {
          blockerId,
          blockedId: userId,
        },
      },
    });

    res.json({ message: 'User unblocked successfully' });
  } catch (error) {
    next(error);
  }
});

// Get blocked users
router.get('/blocked', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const userId = req.userId!;

    const blocks = await prisma.block.findMany({
      where: { blockerId: userId },
      include: {
        blocked: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
      },
    });

    res.json(
      blocks.map((block) => ({
        id: block.blocked.id,
        username: block.blocked.username,
        profilePhotoUrl: block.blocked.profilePhotoUrl,
        blockedAt: block.createdAt.toISOString(),
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Report a user
router.post('/report', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { reportedId, reason, description } = req.body;
    const reporterId = req.userId!;

    // Check if trying to report self
    if (reportedId === reporterId) {
      res.status(400).json({ error: 'Cannot report yourself' });
      return;
    }

    // Check if user exists
    const reportedUser = await prisma.user.findUnique({
      where: { id: reportedId },
    });

    if (!reportedUser) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    // Check if already reported
    const existingReport = await prisma.report.findFirst({
      where: {
        reporterId,
        reportedId,
        status: 'pending',
      },
    });

    if (existingReport) {
      res.status(400).json({ error: 'Already reported this user' });
      return;
    }

    // Validate reason
    const validReasons = ['spam', 'inappropriate', 'harassment', 'fake_profile'];
    if (!validReasons.includes(reason)) {
      res.status(400).json({ error: 'Invalid reason' });
      return;
    }

    // Create report
    const report = await prisma.report.create({
      data: {
        reporterId,
        reportedId,
        reason,
        description,
      },
    });

    res.status(201).json({
      id: report.id,
      status: report.status,
      createdAt: report.createdAt.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

// Get reports (admin only)
router.get('/reports', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { status = 'pending' } = req.query;

    const reports = await prisma.report.findMany({
      where: {
        status: status as string,
      },
      include: {
        reporter: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
        reported: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    });

    res.json(
      reports.map((report) => ({
        id: report.id,
        reporter: {
          id: report.reporter.id,
          username: report.reporter.username,
          profilePhotoUrl: report.reporter.profilePhotoUrl,
        },
        reported: {
          id: report.reported.id,
          username: report.reported.username,
          profilePhotoUrl: report.reported.profilePhotoUrl,
        },
        reason: report.reason,
        description: report.description,
        status: report.status,
        createdAt: report.createdAt.toISOString(),
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Update report status (admin only)
router.patch('/reports/:reportId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { reportId } = req.params;
    const { status } = req.body;

    const validStatuses = ['pending', 'reviewed', 'resolved', 'dismissed'];
    if (!validStatuses.includes(status)) {
      res.status(400).json({ error: 'Invalid status' });
      return;
    }

    const report = await prisma.report.update({
      where: { id: reportId },
      data: {
        status,
        reviewedAt: new Date(),
      },
    });

    res.json({
      id: report.id,
      status: report.status,
      reviewedAt: report.reviewedAt?.toISOString(),
    });
  } catch (error) {
    next(error);
  }
});

export default router;
