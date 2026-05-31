import { Router, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import prisma from '../../config/database';
import { authMiddleware, AuthRequest } from '../../middleware/auth.middleware';

const router = Router();

// Get all chats for current user
router.get('/', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const userId = req.userId!;

    // Get all matches (which represent potential chats)
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
        messages: {
          orderBy: { createdAt: 'desc' },
          take: 1,
        },
      },
      orderBy: { createdAt: 'desc' },
    });

    // Format chats
    const chats = matches.map((match) => {
      const otherUser = match.user1Id === userId ? match.user2 : match.user1;
      const lastMessage = match.messages[0];

      return {
        id: match.id,
        otherUser: {
          id: otherUser.id,
          username: otherUser.username,
          profilePhotoUrl: otherUser.profilePhotoUrl,
          isVerified: otherUser.isVerified,
        },
        lastMessage: lastMessage
          ? {
              id: lastMessage.id,
              senderId: lastMessage.senderId,
              encryptedContent: lastMessage.encryptedContent,
              contentType: lastMessage.contentType,
              createdAt: lastMessage.createdAt.toISOString(),
            }
          : null,
        unreadCount: 0, // TODO: Calculate unread count
        createdAt: match.createdAt.toISOString(),
        updatedAt: match.updatedAt.toISOString(),
      };
    });

    res.json(chats);
  } catch (error) {
    next(error);
  }
});

// Get messages for a chat
router.get('/:chatId/messages', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { chatId } = req.params;
    const { limit = '50', before } = req.query;

    // Get the match to find the other user
    const match = await prisma.match.findUnique({
      where: { id: chatId },
    });

    if (!match) {
      res.status(404).json({ error: 'Chat not found' });
      return;
    }

    const userId = req.userId!;
    const otherUserId = match.user1Id === userId ? match.user2Id : match.user1Id;

    // Get messages between users
    const messages = await prisma.message.findMany({
      where: {
        OR: [
          { senderId: userId, receiverId: otherUserId },
          { senderId: otherUserId, receiverId: userId },
        ],
        ...(before && {
          createdAt: {
            lt: new Date(before as string),
          },
        }),
      },
      orderBy: { createdAt: 'desc' },
      take: parseInt(limit as string, 10),
      include: {
        sender: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
      },
    });

    res.json(
      messages.reverse().map((msg) => ({
        id: msg.id,
        senderId: msg.senderId,
        receiverId: msg.receiverId,
        encryptedContent: msg.encryptedContent,
        contentType: msg.contentType,
        isRead: msg.isRead,
        createdAt: msg.createdAt.toISOString(),
        senderUsername: msg.sender.username,
        senderPhotoUrl: msg.sender.profilePhotoUrl,
      }))
    );
  } catch (error) {
    next(error);
  }
});

// Send a message
router.post('/:receiverId/messages', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { receiverId } = req.params;
    const { content, contentType = 'text', encryptedContent } = req.body;
    const senderId = req.userId!;

    // Check if receiver exists
    const receiver = await prisma.user.findUnique({
      where: { id: receiverId },
    });

    if (!receiver) {
      res.status(404).json({ error: 'Receiver not found' });
      return;
    }

    // Check if they are matched
    const match = await prisma.match.findFirst({
      where: {
        OR: [
          { user1Id: senderId, user2Id: receiverId },
          { user1Id: receiverId, user2Id: senderId },
        ],
      },
    });

    if (!match) {
      res.status(403).json({ error: 'You can only message matched users' });
      return;
    }

    // Create message
    const message = await prisma.message.create({
      data: {
        senderId,
        receiverId,
        encryptedContent: encryptedContent || content,
        contentType,
      },
      include: {
        sender: {
          select: {
            id: true,
            username: true,
            profilePhotoUrl: true,
          },
        },
      },
    });

    res.status(201).json({
      id: message.id,
      senderId: message.senderId,
      receiverId: message.receiverId,
      encryptedContent: message.encryptedContent,
      contentType: message.contentType,
      isRead: message.isRead,
      createdAt: message.createdAt.toISOString(),
      senderUsername: message.sender.username,
      senderPhotoUrl: message.sender.profilePhotoUrl,
    });
  } catch (error) {
    next(error);
  }
});

// Mark messages as read
router.post('/:chatId/read', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { chatId } = req.params;
    const userId = req.userId!;

    // Get the match to find the other user
    const match = await prisma.match.findUnique({
      where: { id: chatId },
    });

    if (!match) {
      res.status(404).json({ error: 'Chat not found' });
      return;
    }

    const otherUserId = match.user1Id === userId ? match.user2Id : match.user1Id;

    // Mark messages as read
    await prisma.message.updateMany({
      where: {
        senderId: otherUserId,
        receiverId: userId,
        isRead: false,
      },
      data: { isRead: true },
    });

    res.json({ message: 'Messages marked as read' });
  } catch (error) {
    next(error);
  }
});

// Initialize encryption keys
router.post('/encryption/init', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { publicKey } = req.body;
    const userId = req.userId!;

    // TODO: Store encryption public key
    // For now, just return success
    res.json({ message: 'Encryption initialized' });
  } catch (error) {
    next(error);
  }
});

// Get public key for a user
router.get('/encryption/public-key/:userId', authMiddleware, async (req: AuthRequest, res: Response, next: NextFunction) => {
  try {
    const { userId } = req.params;

    // TODO: Get public key from storage
    // For now, return placeholder
    res.json({ publicKey: '' });
  } catch (error) {
    next(error);
  }
});

export default router;
