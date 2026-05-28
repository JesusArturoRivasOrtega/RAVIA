import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import * as admin from 'firebase-admin';
import { v4 as uuid } from 'uuid';
import { FirebaseService } from '../firebase/firebase.service';
import { MissingPerson, MissingPersonStatus, Sighting } from './missing-person.entity';
import { User } from '../users/user.entity';
import { getWithIndexFallback } from '../common/firestore-query.util';
import { CreateMissingPersonDto, ReportSightingDto } from './dto/missing-person.dto';

const COLLECTION = 'missing_persons';

@Injectable()
export class MissingPersonsService {
  private readonly logger = new Logger(MissingPersonsService.name);

  constructor(private readonly firebase: FirebaseService) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  private docToPerson(id: string, data: admin.firestore.DocumentData): MissingPerson {
    return {
      id,
      name: data.name,
      age: data.age,
      photoUrl: data.photoUrl,
      lastSeenLocation: data.lastSeenLocation,
      lastSeenLat: data.lastSeenLat,
      lastSeenLng: data.lastSeenLng,
      clothing: data.clothing,
      distinctiveSigns: data.distinctiveSigns,
      description: data.description,
      contactInfo: data.contactInfo,
      status: data.status ?? MissingPersonStatus.ACTIVE,
      reportedBy: data.reportedBy,
      sightings: (data.sightings ?? []).map((s: any) => ({
        ...s,
        createdAt: s.createdAt?.toDate?.() ?? new Date(),
      })),
      createdAt: data.createdAt?.toDate?.() ?? new Date(),
      updatedAt: data.updatedAt?.toDate?.() ?? new Date(),
    };
  }

  async findAll(): Promise<MissingPerson[]> {
    const { snapshot } = await getWithIndexFallback(
      this.col
        .where('status', '==', MissingPersonStatus.ACTIVE)
        .orderBy('createdAt', 'desc')
        .limit(50),
      this.col,
      this.logger,
      'missingPersons.findAll',
    );

    return snapshot.docs
      .map((d) => this.docToPerson(d.id, d.data()))
      .filter((person) => person.status === MissingPersonStatus.ACTIVE)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
      .slice(0, 50);
  }

  async findForReview(limit = 50): Promise<MissingPerson[]> {
    const { snapshot } = await getWithIndexFallback(
      this.col
        .where('status', '==', MissingPersonStatus.PENDING_REVIEW)
        .orderBy('createdAt', 'desc')
        .limit(limit),
      this.col,
      this.logger,
      'missingPersons.findForReview',
    );

    return snapshot.docs
      .map((d) => this.docToPerson(d.id, d.data()))
      .filter((person) => person.status === MissingPersonStatus.PENDING_REVIEW)
      .sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime())
      .slice(0, limit);
  }

  async findById(id: string): Promise<MissingPerson> {
    const doc = await this.col.doc(id).get();
    if (!doc.exists) throw new NotFoundException(`Missing person report ${id} not found`);
    return this.docToPerson(doc.id, doc.data());
  }

  async create(dto: CreateMissingPersonDto, author: User): Promise<MissingPerson> {
    const id = uuid();
    const now = admin.firestore.FieldValue.serverTimestamp();

    await this.col.doc(id).set({
      name: dto.name,
      age: dto.age ?? null,
      photoUrl: dto.photoUrl ?? null,
      lastSeenLocation: dto.lastSeenLocation,
      lastSeenLat: dto.lastSeenLat ?? null,
      lastSeenLng: dto.lastSeenLng ?? null,
      clothing: dto.clothing ?? null,
      distinctiveSigns: dto.distinctiveSigns ?? null,
      description: dto.description,
      contactInfo: dto.contactInfo,
      status: MissingPersonStatus.PENDING_REVIEW,
      reportedBy: author.id,
      sightings: [],
      createdAt: now,
      updatedAt: now,
    });

    return this.findById(id);
  }

  async reportSighting(personId: string, dto: ReportSightingDto, user: User): Promise<MissingPerson> {
    const sighting: Sighting = {
      id: uuid(),
      reportedBy: user.id,
      lat: dto.lat,
      lng: dto.lng,
      createdAt: new Date(),
    };
    if (dto.comment !== undefined) sighting.comment = dto.comment;
    if (dto.photoUrl !== undefined) sighting.photoUrl = dto.photoUrl;

    await this.col.doc(personId).update({
      sightings: admin.firestore.FieldValue.arrayUnion(sighting),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

    return this.findById(personId);
  }

  async updateStatus(id: string, status: MissingPersonStatus): Promise<MissingPerson> {
    await this.col.doc(id).update({ status, updatedAt: admin.firestore.FieldValue.serverTimestamp() });
    return this.findById(id);
  }
}
