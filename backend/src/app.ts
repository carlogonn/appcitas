import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { createServer } from 'http';
import { Server as SocketIOServer } from 'socket.io';
import env from './config/env';
import corsOptions from './config/cors';
import { apiLimiter } from './middleware/rateLimit.middleware';
import { errorHandler } from './middleware/errorHandler';
import authRoutes from './modules/auth/auth.routes';
import userRoutes from './modules/users/users.routes';
import photosRoutes from './modules/users/photos.routes';
import verificationRoutes from './modules/verification/verification.routes';
import chatRoutes from './modules/chat/chat.routes';
import communityRoutes from './modules/community/community.routes';
import matchRoutes from './modules/matches/matches.routes';
import moderationRoutes from './modules/moderation/moderation.routes';
import blocksReportsRoutes from './modules/moderation/blocks-reports.routes';
import icebreakerRoutes from './modules/icebreaker/icebreaker.routes';
import adminRoutes from './modules/admin/admin.routes';

const app = express();
const httpServer = createServer(app);

// Socket.IO setup
const io = new SocketIOServer(httpServer, {
  cors: {
    origin: env.FRONTEND_URL,
    methods: ['GET', 'POST'],
    credentials: true,
  },
});

// Middleware
app.use(helmet());
app.use(cors(corsOptions));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(apiLimiter);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API Routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/users', userRoutes);
app.use('/api/v1/users', photosRoutes);
app.use('/api/v1/verification', verificationRoutes);
app.use('/api/v1/chat', chatRoutes);
app.use('/api/v1/chat/community', communityRoutes);
app.use('/api/v1/matches', matchRoutes);
app.use('/api/v1/moderation', moderationRoutes);
app.use('/api/v1/moderation', blocksReportsRoutes);
app.use('/api/v1/icebreaker', icebreakerRoutes);
app.use('/api/v1/admin', adminRoutes);

// Static files (uploads)
app.use('/uploads', express.static('uploads'));

// Socket.IO connection handling
io.on('connection', (socket) => {
  console.log('User connected:', socket.id);

  socket.on('join_chat', (chatId: string) => {
    socket.join(chatId);
  });

  socket.on('leave_chat', (chatId: string) => {
    socket.leave(chatId);
  });

  socket.on('send_message', (data) => {
    io.to(data.chatId).emit('new_message', data);
  });

  socket.on('disconnect', () => {
    console.log('User disconnected:', socket.id);
  });
});

// Error handling
app.use(errorHandler);

// Start server
const PORT = parseInt(env.PORT, 10);

httpServer.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
  console.log(`Environment: ${env.NODE_ENV}`);
});

export { app, io };
