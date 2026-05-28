import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { v4 as uuid } from 'uuid';
import { FirebaseService } from '../firebase/firebase.service';
import { Alert, AlertSeverity } from './alert.entity';
import { NotificationsService } from '../notifications/notifications.service';
import { User } from '../users/user.entity';
import { getWithIndexFallback } from '../common/firestore-query.util';

const COLLECTION = 'alerts';

export class CreateAlertDto {
  title: string;
  description: string;
  severity: AlertSeverity;
  affectedZones?: string[];
  expiresAt?: string;
}

@Injectable()
export class AlertsService {
  private readonly logger = new Logger(AlertsService.name);

  constructor(
    private readonly firebase: FirebaseService,
    private readonly notifications: NotificationsService,
  ) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  private docToAlert(id: string, data: admin.firestore.DocumentData): Alert {
    return {
      id,
      title: data.title,
      description: data.description,
      severity: data.severity,
      affectedZones: data.affectedZones ?? [],
      isActive: data.isActive ?? true,
      authorId: data.authorId,
      expiresAt: data.expiresAt?.toDate?.(),
      createdAt: data.createdAt?.toDate?.() ?? new Date(),
      updatedAt: data.updatedAt?.toDate?.() ?? new Date(),
    };
  }

  async findActive(): Promise<Alert[]> {
    const { snapshot } = await getWithIndexFallback(
      this.col
        .where('isActive', '==', true)
        .orderBy('createdAt', 'desc')
        .limit(50),
      this.col,
      this.logger,
      'alerts.findActive',
    );

    return snapshot.docs
      .map((d) => this.docToAlert(d.id, d.data()))
      .filter((alert) => alert.isActive)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
      .slice(0, 50);
  }

  async findAll(limit = 50): Promise<Alert[]> {
    const snap = await this.col.orderBy('createdAt', 'desc').limit(limit).get();
    return snap.docs.map((d) => this.docToAlert(d.id, d.data()));
  }

  async findById(id: string): Promise<Alert> {
    const doc = await this.col.doc(id).get();
    if (!doc.exists) throw new NotFoundException(`Alert ${id} not found`);
    return this.docToAlert(doc.id, doc.data());
  }

  async create(dto: CreateAlertDto, author: User): Promise<Alert> {
    const id = uuid();
    const now = admin.firestore.FieldValue.serverTimestamp();

    const data = {
      title: dto.title,
      description: dto.description,
      severity: dto.severity,
      affectedZones: dto.affectedZones ?? [],
      isActive: true,
      authorId: author.id,
      expiresAt: dto.expiresAt ? new Date(dto.expiresAt) : null,
      createdAt: now,
      updatedAt: now,
    };

    await this.col.doc(id).set(data);

    // Push notification for critical/urgent alerts
    if (dto.severity === AlertSeverity.CRITICAL || dto.severity === AlertSeverity.URGENT) {
      await this.notifications.broadcastCriticalAlert(dto.title, dto.description, id);
    }

    return this.findById(id);
  }

  async deactivate(id: string): Promise<Alert> {
    await this.col.doc(id).update({
      isActive: false,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return this.findById(id);
  }
}
