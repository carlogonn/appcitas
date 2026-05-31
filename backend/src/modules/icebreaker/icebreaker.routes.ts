import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get all topics (optionally filtered by category)
router.get('/', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { category } = req.query;

    const topics = await prisma.icebreakerTopic.findMany({
      where: {
        isActive: true,
        ...(category && { category: category as string }),
      },
      orderBy: { createdAt: 'asc' },
    });

    res.json(
      topics.map((topic) => ({
        id: topic.id,
        topicText: topic.topicText,
        category: topic.category,
        isActive: topic.isActive,
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Get random topic
router.get('/random', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const topics = await prisma.icebreakerTopic.findMany({
      where: { isActive: true },
    });

    if (topics.length === 0) {
      res.status(404).json({ error: 'No topics available' });
      return;
    }

    const randomIndex = Math.floor(Math.random() * topics.length);
    const topic = topics[randomIndex];

    res.json({
      id: topic.id,
      topicText: topic.topicText,
      category: topic.category,
    });
  } catch (error) {
    next(error);
  }
});

// Get daily topic (same for all users on same day)
router.get('/daily', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const topics = await prisma.icebreakerTopic.findMany({
      where: { isActive: true },
    });

    if (topics.length === 0) {
      res.status(404).json({ error: 'No topics available' });
      return;
    }

    // Use day of year to get consistent daily topic
    const now = new Date();
    const start = new Date(now.getFullYear(), 0, 0);
    const diff = now.getTime() - start.getTime();
    const oneDay = 1000 * 60 * 60 * 24;
    const dayOfYear = Math.floor(diff / oneDay);

    const index = dayOfYear % topics.length;
    const topic = topics[index];

    res.json({
      id: topic.id,
      topicText: topic.topicText,
      category: topic.category,
    });
  } catch (error) {
    next(error);
  }
});

// Get categories
router.get('/categories', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const categories = await prisma.icebreakerTopic.findMany({
      where: { isActive: true },
      distinct: ['category'],
      select: { category: true },
    });

    res.json(categories.map((c) => c.category));
  } catch (error) {
    next(error);
  }
});

// Get topics by category
router.get('/category/:category', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { category } = req.params;

    const topics = await prisma.icebreakerTopic.findMany({
      where: {
        category,
        isActive: true,
      },
      orderBy: { createdAt: 'asc' },
    });

    res.json(
      topics.map((topic) => ({
        id: topic.id,
        topicText: topic.topicText,
        category: topic.category,
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Create topic (admin only)
router.post('/', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { topicText, category } = req.body;

    if (!topicText || !category) {
      res.status(400).json({ error: 'topicText and category are required' });
      return;
    }

    const topic = await prisma.icebreakerTopic.create({
      data: {
        topicText,
        category,
      },
    });

    res.status(201).json({
      id: topic.id,
      topicText: topic.topicText,
      category: topic.category,
      isActive: topic.isActive,
    });
  } catch (error) {
    next(error);
  }
});

// Update topic (admin only)
router.patch('/:id', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { id } = req.params;
    const { topicText, category, isActive } = req.body;

    const topic = await prisma.icebreakerTopic.update({
      where: { id },
      data: {
        ...(topicText && { topicText }),
        ...(category && { category }),
        ...(isActive !== undefined && { isActive }),
      },
    });

    res.json({
      id: topic.id,
      topicText: topic.topicText,
      category: topic.category,
      isActive: topic.isActive,
    });
  } catch (error) {
    next(error);
  }
});

// Delete topic (admin only)
router.delete('/:id', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    // TODO: Check if user is admin
    const { id } = req.params;

    await prisma.icebreakerTopic.delete({
      where: { id },
    });

    res.json({ message: 'Topic deleted' });
  } catch (error) {
    next(error);
  }
});

export default router;
