import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get dashboard stats
router.get('/stats', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const [
      totalUsers,
      activeUsers,
      totalMatches,
      pendingReports,
      pendingVerifications,
      totalMessages,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.user.count({
        where: {
          updatedAt: {
            gte: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // Last 7 days
          },
        },
      }),
      prisma.match.count(),
      prisma.report.count({ where: { status: 'pending' } }),
      prisma.verification.count({ where: { status: 'pending' } }),
      prisma.message.count(),
    ]);

    res.json({
      totalUsers,
      activeUsers,
      totalMatches,
      pendingReports,
      pendingVerifications,
      totalMessages,
    });
  } catch (error) {
    next(error);
  }
});

// Get all users (admin)
router.get('/users', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { page = '1', limit = '20', search } = req.query;
    const skip = (parseInt(page as string, 10) - 1) * parseInt(limit as string, 10);

    const where = search
      ? {
          OR: [
            { username: { contains: search as string, mode: 'insensitive' as const } },
            { email: { contains: search as string, mode: 'insensitive' as const } },
          ],
        }
      : {};

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        where,
        select: {
          id: true,
          email: true,
          username: true,
          isVerified: true,
          createdAt: true,
          updatedAt: true,
        },
        skip,
        take: parseInt(limit as string, 10),
        orderBy: { createdAt: 'desc' },
      }),
      prisma.user.count({ where }),
    ]);

    res.json({
      users,
      total,
      page: parseInt(page as string, 10),
      totalPages: Math.ceil(total / parseInt(limit as string, 10)),
    });
  } catch (error) {
    next(error);
  }
});

// Get all reports (admin)
router.get('/reports', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { status = 'pending' } = req.query;

    const reports = await prisma.report.findMany({
      where: { status: status as string },
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

    res.json(reports);
  } catch (error) {
    next(error);
  }
});

// Ban user
router.post('/users/:userId/ban', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { userId } = req.params;

    // TODO: Implement user banning (soft delete or status change)
    // For now, just delete the user
    await prisma.user.delete({ where: { id: userId } });

    res.json({ message: 'User banned successfully' });
  } catch (error) {
    next(error);
  }
});

// Update report status
router.patch('/reports/:reportId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { reportId } = req.params;
    const { status } = req.body;

    const report = await prisma.report.update({
      where: { id: reportId },
      data: {
        status,
        reviewedAt: new Date(),
      },
    });

    res.json(report);
  } catch (error) {
    next(error);
  }
});

// Get app settings
router.get('/settings', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const settings = await prisma.appSetting.findMany();
    res.json(settings);
  } catch (error) {
    next(error);
  }
});

// Update app settings
router.put('/settings', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { settings } = req.body;

    for (const [key, value] of Object.entries(settings)) {
      await prisma.appSetting.upsert({
        where: { settingKey: key },
        update: { settingValue: value as string },
        create: { settingKey: key, settingValue: value as string },
      });
    }

    res.json({ message: 'Settings updated successfully' });
  } catch (error) {
    next(error);
  }
});

export default router;
