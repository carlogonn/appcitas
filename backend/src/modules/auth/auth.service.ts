import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { v4 as uuidv4 } from 'uuid';
import prisma from '../../config/database';
import redis from '../../config/redis';
import env from '../../config/env';
import { AppError } from '../../middleware/errorHandler';
import { RegisterInput, LoginInput } from './auth.validation';

export class AuthService {
  async register(data: RegisterInput) {
    const { email, username, password, birthDate, gender } = data;

    // Check if user exists
    const existingUser = await prisma.user.findFirst({
      where: {
        OR: [{ email }, { username }],
      },
    });

    if (existingUser) {
      throw new AppError(
        existingUser.email === email
          ? 'El email ya está registrado'
          : 'El nombre de usuario ya está en uso',
        409
      );
    }

    // Hash password
    const passwordHash = await bcrypt.hash(password, 12);

    // Create user
    const user = await prisma.user.create({
      data: {
        email,
        username,
        passwordHash,
        birthDate: new Date(birthDate),
        gender,
      },
    });

    // Generate tokens
    const tokens = await this.generateTokens(user.id);

    return {
      user: this.formatUser(user),
      ...tokens,
    };
  }

  async login(data: LoginInput) {
    const { email, password } = data;

    // Find user
    const user = await prisma.user.findUnique({
      where: { email },
    });

    if (!user) {
      throw new AppError('Email o contraseña incorrectos', 401);
    }

    // Verify password
    const isPasswordValid = await bcrypt.compare(password, user.passwordHash);

    if (!isPasswordValid) {
      throw new AppError('Email o contraseña incorrectos', 401);
    }

    // Generate tokens
    const tokens = await this.generateTokens(user.id);

    return {
      user: this.formatUser(user),
      ...tokens,
    };
  }

  async refreshToken(refreshToken: string) {
    try {
      const decoded = jwt.verify(refreshToken, env.JWT_REFRESH_SECRET) as {
        userId: string;
      };

      // Check if refresh token is blacklisted
      const isBlacklisted = await redis.get(`blacklist:${refreshToken}`);
      if (isBlacklisted) {
        throw new AppError('Token inválido', 401);
      }

      // Blacklist old refresh token
      await redis.set(`blacklist:${refreshToken}`, '1', 'EX', 7 * 24 * 60 * 60);

      // Generate new tokens
      const tokens = await this.generateTokens(decoded.userId);

      return tokens;
    } catch (error) {
      if (error instanceof AppError) {
        throw error;
      }
      throw new AppError('Token inválido', 401);
    }
  }

  async logout(userId: string, refreshToken?: string) {
    if (refreshToken) {
      await redis.set(`blacklist:${refreshToken}`, '1', 'EX', 7 * 24 * 60 * 60);
    }
  }

  private async generateTokens(userId: string) {
    const accessToken = jwt.sign({ userId }, env.JWT_SECRET, {
      expiresIn: env.JWT_EXPIRES_IN,
    });

    const refreshToken = jwt.sign({ userId }, env.JWT_REFRESH_SECRET, {
      expiresIn: env.JWT_REFRESH_EXPIRES_IN,
    });

    // Store refresh token in Redis
    await redis.set(
      `refresh:${userId}:${refreshToken}`,
      '1',
      'EX',
      7 * 24 * 60 * 60
    );

    return { accessToken, refreshToken };
  }

  private formatUser(user: any) {
    return {
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
    };
  }
}

export const authService = new AuthService();
