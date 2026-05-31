import { Router, Response, NextFunction } from 'express';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get all community channels
router.get('/channels', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const channels = await prisma.communityChannel.findMany({
      orderBy: { createdAt: 'asc' },
      include: {
        _count: {
          select: { messages: true },
        },
      },
    });

    res.json(
      channels.map((channel) => ({
        id: channel.id,
        name: channel.name,
        description: channel.description,
        memberCount: channel._count.messages,
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Get messages for a channel
router.get('/:channelId/messages', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { channelId } = req.params;
    const { limit = '50', before } = req.query;

    const messages = await prisma.communityMessage.findMany({
      where: {
        channelId,
        ...(before && {
          createdAt: {
            lt: new Date(before as string),
          },
        }),
      },
      orderBy: { createdAt: 'desc' },
      take: parseInt(limit as string, 10),
      include: {
        user: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
            isVerified: true,
          },
        },
        channel: true,
      },
    });

    // Get user locations for distance calculation
    const userIds = [...new Set(messages.map((m) => m.userId))];
    const locations = await prisma.userLocation.findMany({
      where: { userId: { in: userIds } },
    });

    const locationMap = new Map(
      locations.map((loc) => [loc.userId, { latitude: Number(loc.latitude), longitude: Number(loc.longitude) }])
    );

    // Get current user's location
    const currentUserLocation = await prisma.userLocation.findUnique({
      where: { userId: req.userId! },
    });

    res.json(
      messages.reverse().map((msg) => {
        const userLocation = locationMap.get(msg.userId);
        let distance = null;

        if (currentUserLocation && userLocation) {
          distance = calculateDistance(
            Number(currentUserLocation.latitude),
            Number(currentUserLocation.longitude),
            userLocation.latitude,
            userLocation.longitude
          );
        }

        return {
          id: msg.id,
          channelId: msg.channelId,
          userId: msg.userId,
          username: msg.user.username,
          content: msg.content,
          distance,
          createdAt: msg.createdAt.toISOString(),
          senderPhotoUrl: msg.user.profilePhotoUrl,
        };
      })
    );
  } catch (error) {
    next(error);
  }
});

// Send a message to a channel
router.post('/:channelId/messages', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { channelId } = req.params;
    const { content } = req.body;
    const userId = req.userId!;

    // Check if channel exists
    const channel = await prisma.communityChannel.findUnique({
      where: { id: channelId },
    });

    if (!channel) {
      res.status(404).json({ error: 'Channel not found' });
      return;
    }

    // Get user info
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        username: true,
        profilePhotoUrl: true,
        showDistance: true,
      },
    });

    if (!user) {
      res.status(404).json({ error: 'User not found' });
      return;
    }

    // Create message
    const message = await prisma.communityMessage.create({
      data: {
        channelId,
        userId,
        content,
      },
      include: {
        user: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
      },
    });

    // Get distance if user allows
    let distance = null;
    if (user.showDistance) {
      const userLocation = await prisma.userLocation.findUnique({
        where: { userId },
      });

      if (userLocation) {
        // TODO: Calculate distance from center of community or average location
        distance = 0; // Placeholder
      }
    }

    res.status(201).json({
      id: message.id,
      channelId: message.channelId,
      userId: message.userId,
      username: message.user.username,
      content: message.content,
      distance,
      createdAt: message.createdAt.toISOString(),
      senderPhotoUrl: message.user.profilePhotoUrl,
    });
  } catch (error) {
    next(error);
  }
});

// Helper function to calculate distance using Haversine formula
function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Earth's radius in kilometers

  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

function toRad(deg: number): number {
  return deg * (Math.PI / 180);
}

export default router;
