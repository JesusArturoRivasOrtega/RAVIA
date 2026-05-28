import { Injectable, Logger, ConflictException, UnauthorizedException } from '@nestjs/common';
import { FirebaseService } from '../firebase/firebase.service';
import { UsersService } from '../users/users.service';
import { RegisterDto } from './dto/auth.dto';
import { UserRole } from '../users/user.entity';
import * as admin from 'firebase-admin';

@Injectable()
export class AuthService {
  private readonly logger = new Logger(AuthService.name);

  constructor(
    private readonly firebase: FirebaseService,
    private readonly usersService: UsersService,
  ) {}

  /**
   * Registers a new Firebase Auth user and creates the Firestore profile.
   * The actual ID token exchange happens client-side (Android app calls Firebase Auth SDK).
   * This endpoint is used by the admin panel / server-side registration if needed.
   */
  async register(dto: RegisterDto) {
    try {
      const firebaseUser = await this.firebase.auth.createUser({
        email: dto.email,
        password: dto.password,
        displayName: dto.displayName,
      });

      const now = admin.firestore.FieldValue.serverTimestamp();
      await this.firebase.collection('users').doc(firebaseUser.uid).set({
        email: dto.email,
        displayName: dto.displayName,
        role: UserRole.CITIZEN,
        status: 'active',
        reputationPoints: 0,
        reportCount: 0,
        confirmedReports: 0,
        zone: dto.zone ?? null,
        fcmTokens: dto.fcmToken ? [dto.fcmToken] : [],
        createdAt: now,
        updatedAt: now,
        lastLoginAt: now,
      });

      this.logger.log(`Registered new user: ${firebaseUser.uid}`);
      return { uid: firebaseUser.uid, email: firebaseUser.email };
    } catch (error: any) {
      if (error.code === 'auth/email-already-exists') {
        throw new ConflictException('Email already registered');
      }
      throw error;
    }
  }

  /**
   * Verifies a Firebase ID token and returns the user profile.
   * The Android app sends this token in the Authorization header for every request.
   */
  async verifyToken(token: string) {
    try {
      const decoded = await this.firebase.verifyIdToken(token);
      const user = await this.usersService.findOrCreateFromFirebase(decoded);
      return user;
    } catch {
      throw new UnauthorizedException('Invalid token');
    }
  }

  async resetPassword(email: string): Promise<{ message: string }> {
    // Firebase handles password reset via email — generate link server-side
    const link = await this.firebase.auth.generatePasswordResetLink(email);
    this.logger.log(`Password reset link generated for ${email}: ${link}`);
    // In production, send via transactional email service
    return { message: 'Password reset email sent' };
  }
}
