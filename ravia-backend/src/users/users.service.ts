import { BadRequestException, ForbiddenException, Injectable, NotFoundException, Logger } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { FirebaseService } from '../firebase/firebase.service';
import { User, UserRole, UserStatus } from './user.entity';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';

const COLLECTION = 'users';

@Injectable()
export class UsersService {
  private readonly logger = new Logger(UsersService.name);

  constructor(private readonly firebase: FirebaseService) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  private normalizeRole(value: unknown): UserRole {
    const role = String(value ?? '').toLowerCase();
    if (Object.values(UserRole).includes(role as UserRole)) return role as UserRole;
    return UserRole.CITIZEN;
  }

  private normalizeStatus(value: unknown): UserStatus {
    const status = String(value ?? '').toLowerCase();
    if (Object.values(UserStatus).includes(status as UserStatus)) return status as UserStatus;
    return UserStatus.ACTIVE;
  }

  private docToUser(id: string, data: admin.firestore.DocumentData): User {
    return {
      id,
      email: data.email,
      displayName: data.displayName,
      photoUrl: data.photoUrl,
      role: this.normalizeRole(data.role),
      status: this.normalizeStatus(data.status),
      reputationPoints: data.reputationPoints ?? 0,
      reportCount: data.reportCount ?? 0,
      confirmedReports: data.confirmedReports ?? 0,
      zone: data.zone,
      fcmTokens: data.fcmTokens ?? [],
      createdAt: data.createdAt?.toDate?.() ?? new Date(),
      updatedAt: data.updatedAt?.toDate?.() ?? new Date(),
      lastLoginAt: data.lastLoginAt?.toDate?.(),
    };
  }

  async findById(id: string): Promise<User> {
    const doc = await this.col.doc(id).get();
    if (!doc.exists) throw new NotFoundException(`User ${id} not found`);
    return this.docToUser(doc.id, doc.data());
  }

  async findAll(limit = 50, startAfter?: string): Promise<User[]> {
    let query = this.col.orderBy('createdAt', 'desc').limit(limit);
    if (startAfter) {
      const cursor = await this.col.doc(startAfter).get();
      if (cursor.exists) query = query.startAfter(cursor);
    }
    const snap = await query.get();
    return snap.docs.map((d) => this.docToUser(d.id, d.data()));
  }

  async findOrCreateFromFirebase(decoded: admin.auth.DecodedIdToken): Promise<User> {
    const ref = this.col.doc(decoded.uid);
    const doc = await ref.get();

    if (doc.exists) {
      // Update last login
      await ref.update({ lastLoginAt: admin.firestore.FieldValue.serverTimestamp() });
      return this.docToUser(doc.id, doc.data());
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    const newUser = {
      email: decoded.email ?? '',
      displayName: decoded.name ?? decoded.email?.split('@')[0] ?? 'Usuario',
      photoUrl: decoded.picture ?? null,
      role: UserRole.CITIZEN,
      status: UserStatus.ACTIVE,
      reputationPoints: 0,
      reportCount: 0,
      confirmedReports: 0,
      fcmTokens: [],
      createdAt: now,
      updatedAt: now,
      lastLoginAt: now,
    };

    await ref.set(newUser);
    this.logger.log(`New user created: ${decoded.uid}`);

    return this.docToUser(decoded.uid, { ...newUser, createdAt: new Date(), updatedAt: new Date(), lastLoginAt: new Date() });
  }

  async update(id: string, dto: UpdateUserDto): Promise<User> {
    const ref = this.col.doc(id);
    const doc = await ref.get();
    if (!doc.exists) throw new NotFoundException(`User ${id} not found`);

    const updates: Record<string, any> = {
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    if (dto.displayName) updates.displayName = dto.displayName;
    if (dto.photoUrl !== undefined) updates.photoUrl = dto.photoUrl;
    if (dto.zone !== undefined) updates.zone = dto.zone;

    if (dto.fcmToken) {
      updates.fcmTokens = admin.firestore.FieldValue.arrayUnion(dto.fcmToken);
    }

    await ref.update(updates);
    return this.findById(id);
  }

  async updateRole(id: string, role: UserRole, actor: User): Promise<User> {
    const nextRole = this.normalizeRole(role);
    const ref = this.col.doc(id);
    const doc = await ref.get();
    if (!doc.exists) throw new NotFoundException(`User ${id} not found`);
    if (actor.id === id && nextRole !== actor.role) {
      throw new BadRequestException('Admins cannot change their own role');
    }

    await ref.update({ role: nextRole, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return this.findById(id);
  }

  async updateStatus(id: string, status: UserStatus, actor: User): Promise<User> {
    const nextStatus = this.normalizeStatus(status);
    const ref = this.col.doc(id);
    const doc = await ref.get();
    if (!doc.exists) throw new NotFoundException(`User ${id} not found`);
    const target = this.docToUser(doc.id, doc.data());

    if (actor.id === id && nextStatus !== UserStatus.ACTIVE) {
      throw new BadRequestException('Users cannot suspend or ban their own account');
    }

    if (actor.role !== UserRole.ADMIN) {
      if (target.role !== UserRole.CITIZEN) {
        throw new ForbiddenException('Moderators can only update citizen accounts');
      }
      if (nextStatus === UserStatus.BANNED) {
        throw new ForbiddenException('Only admins can ban users');
      }
    }

    await ref.update({ status: nextStatus, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return this.findById(id);
  }

  async addReputationPoints(id: string, points: number): Promise<void> {
    await this.col.doc(id).update({
      reputationPoints: admin.firestore.FieldValue.increment(points),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }

  async incrementReportCount(id: string): Promise<void> {
    await this.col.doc(id).update({
      reportCount: admin.firestore.FieldValue.increment(1),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }

  async incrementConfirmedReports(id: string): Promise<void> {
    if (!id) return;
    await this.col.doc(id).update({
      confirmedReports: admin.firestore.FieldValue.increment(1),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }

  async removeFcmToken(id: string, token: string): Promise<void> {
    await this.col.doc(id).update({
      fcmTokens: admin.firestore.FieldValue.arrayRemove(token),
    });
  }
}
