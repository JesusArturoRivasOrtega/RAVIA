import { Injectable, NotFoundException } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { v4 as uuid } from 'uuid';
import { FirebaseService } from '../firebase/firebase.service';
import { RiskZone, RiskLevel } from './risk-zone.entity';
import { User } from '../users/user.entity';

const COLLECTION = 'risk_zones';

export class CreateRiskZoneDto {
  name: string;
  description: string;
  riskLevel: RiskLevel;
  centerLat: number;
  centerLng: number;
  radiusMeters: number;
}

@Injectable()
export class RiskZonesService {
  constructor(private readonly firebase: FirebaseService) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  private docToZone(id: string, data: admin.firestore.DocumentData): RiskZone {
    return {
      id,
      name: data.name,
      description: data.description,
      riskLevel: data.riskLevel,
      centerLat: data.centerLat,
      centerLng: data.centerLng,
      radiusMeters: data.radiusMeters,
      reportCount: data.reportCount ?? 0,
      isActive: data.isActive ?? true,
      authorId: data.authorId,
      createdAt: data.createdAt?.toDate?.() ?? new Date(),
      updatedAt: data.updatedAt?.toDate?.() ?? new Date(),
    };
  }

  async findAll(): Promise<RiskZone[]> {
    const snap = await this.col.where('isActive', '==', true).get();
    return snap.docs.map((d) => this.docToZone(d.id, d.data()));
  }

  async findById(id: string): Promise<RiskZone> {
    const doc = await this.col.doc(id).get();
    if (!doc.exists) throw new NotFoundException(`Risk zone ${id} not found`);
    return this.docToZone(doc.id, doc.data());
  }

  async create(dto: CreateRiskZoneDto, author: User): Promise<RiskZone> {
    const id = uuid();
    const now = admin.firestore.FieldValue.serverTimestamp();

    await this.col.doc(id).set({
      ...dto,
      reportCount: 0,
      isActive: true,
      authorId: author.id,
      createdAt: now,
      updatedAt: now,
    });

    return this.findById(id);
  }

  async update(id: string, dto: Partial<CreateRiskZoneDto>): Promise<RiskZone> {
    await this.col.doc(id).update({ ...dto, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return this.findById(id);
  }

  async deactivate(id: string): Promise<RiskZone> {
    await this.col.doc(id).update({ isActive: false, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return this.findById(id);
  }

  async incrementReportCount(id: string): Promise<void> {
    await this.col.doc(id).update({
      reportCount: admin.firestore.FieldValue.increment(1),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }
}
