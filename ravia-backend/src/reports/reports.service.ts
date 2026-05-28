import {
  Injectable, NotFoundException, ForbiddenException, BadRequestException, Logger
} from '@nestjs/common';
import * as admin from 'firebase-admin';
import { v4 as uuid } from 'uuid';
import { FirebaseService } from '../firebase/firebase.service';
import { Report, ReportSourceProfile, ReportStatus, ReportPriority, ReportStatusHistory, ReportCategory } from './report.entity';
import { CreateReportDto } from './dto/create-report.dto';
import { UpdateReportDto, UpdateReportStatusDto, QueryReportsDto } from './dto/update-report.dto';
import { AiAnalysisService } from '../ai-analysis/ai-analysis.service';
import { NotificationsService } from '../notifications/notifications.service';
import { UsersService } from '../users/users.service';
import { UserRole, UserStatus } from '../users/user.entity';
import { User } from '../users/user.entity';
import { getWithIndexFallback } from '../common/firestore-query.util';
import { AlertSeverity } from '../alerts/alert.entity';

const CRITICAL_CATEGORIES = new Set<ReportCategory>([
  ReportCategory.FIRE,
  ReportCategory.GAS_LEAK,
  ReportCategory.MEDICAL,
  ReportCategory.MISSING_PERSON,
]);

const ACTIVE_REPORT_STATUSES: ReportStatus[] = [
  ReportStatus.PENDING,
  ReportStatus.VERIFYING,
  ReportStatus.CONFIRMED,
  ReportStatus.IN_PROGRESS,
  ReportStatus.CRITICAL,
];

// Allowed status transitions for moderator/admin actions.
const STATUS_TRANSITIONS: Record<ReportStatus, ReportStatus[]> = {
  [ReportStatus.PENDING]:     [ReportStatus.VERIFYING, ReportStatus.CONFIRMED, ReportStatus.CRITICAL, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED],
  [ReportStatus.VERIFYING]:   [ReportStatus.CONFIRMED, ReportStatus.CRITICAL, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED],
  [ReportStatus.CONFIRMED]:   [ReportStatus.CRITICAL, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED],
  [ReportStatus.CRITICAL]:    [ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED],
  [ReportStatus.IN_PROGRESS]: [ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED],
  // Terminal states cannot transition further
  [ReportStatus.RESOLVED]:    [],
  [ReportStatus.FALSE]:       [],
  [ReportStatus.DUPLICATED]:  [],
};

const COLLECTION = 'reports';

@Injectable()
export class ReportsService {
  private readonly logger = new Logger(ReportsService.name);

  constructor(
    private readonly firebase: FirebaseService,
    private readonly aiAnalysis: AiAnalysisService,
    private readonly notifications: NotificationsService,
    private readonly usersService: UsersService,
  ) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  private get alertsCol() {
    return this.firebase.collection('alerts');
  }

  private buildSourceProfile(author: User): ReportSourceProfile {
    const reportScore = Math.min(author.reportCount, 50) / 50;
    const confirmedScore = Math.min(author.confirmedReports, 25) / 25;
    const reputationScore = Math.min(Math.max(author.reputationPoints, 0), 500) / 500;
    const roleBoost = author.role === UserRole.ADMIN ? 0.08 : author.role === UserRole.MODERATOR ? 0.05 : 0;
    const trustScore = Math.min(0.98, 0.45 + reputationScore * 0.25 + reportScore * 0.12 + confirmedScore * 0.10 + roleBoost);

    return {
      displayName: author.displayName,
      role: author.role,
      reputationPoints: author.reputationPoints,
      reportCount: author.reportCount,
      confirmedReports: author.confirmedReports,
      trustScore: Number(trustScore.toFixed(2)),
    };
  }

  private sanitizeMedia(media = []) {
    return media.map((item: any) => {
      const clean: Record<string, any> = {
        id: item.id,
        url: item.url,
        type: item.type,
      };
      if (item.thumbnailUrl !== undefined && item.thumbnailUrl !== null) clean.thumbnailUrl = item.thumbnailUrl;
      return clean;
    });
  }

  private normalizeAiAnalysis(data: any) {
    if (!data) return null;
    return {
      ...data,
      analyzedAt: data.analyzedAt?.toDate?.() ?? data.analyzedAt ?? null,
    };
  }

  private docToReport(id: string, data: admin.firestore.DocumentData): Report {
    return {
      id,
      title: data.title,
      description: data.description,
      category: data.category,
      status: data.status,
      priority: data.priority ?? ReportPriority.MEDIUM,
      location: data.location,
      address: data.address,
      authorId: data.authorId,
      isAnonymous: data.isAnonymous ?? false,
      sourceProfile: data.sourceProfile,
      media: data.media ?? [],
      confirmCount: data.confirmCount ?? 0,
      falseCount: data.falseCount ?? 0,
      duplicateCount: data.duplicateCount ?? 0,
      urgentCount: data.urgentCount ?? 0,
      resolvedSignalCount: data.resolvedSignalCount ?? 0,
      weightedConfirmScore: data.weightedConfirmScore ?? 0,
      weightedFalseScore: data.weightedFalseScore ?? 0,
      weightedDuplicateScore: data.weightedDuplicateScore ?? 0,
      weightedResolvedScore: data.weightedResolvedScore ?? 0,
      aiAnalysis: this.normalizeAiAnalysis(data.aiAnalysis),
      statusHistory: (data.statusHistory ?? []).map((h: any) => ({
        ...h,
        timestamp: h.timestamp?.toDate?.() ?? new Date(),
      })),
      resolvedAt: data.resolvedAt?.toDate?.(),
      createdAt: data.createdAt?.toDate?.() ?? new Date(),
      updatedAt: data.updatedAt?.toDate?.() ?? new Date(),
    };
  }

  private toListReport(report: Report): Report {
    return {
      ...report,
      media: [],
      statusHistory: [],
    };
  }

  private sortNewestFirst(reports: Report[]): Report[] {
    return reports.sort((a, b) => b.createdAt.getTime() - a.createdAt.getTime());
  }

  private async publishCriticalReportAlert(reportId: string, report: Pick<Report, 'title' | 'description' | 'category' | 'location' | 'authorId'>): Promise<void> {
    const now = admin.firestore.FieldValue.serverTimestamp();
    const alertId = `report_${reportId}_critical`;
    await this.alertsCol.doc(alertId).set(
      {
        title: report.title || 'Reporte critico cercano',
        description: report.description || `Nuevo reporte critico de ${report.category} cerca de ti`,
        severity: AlertSeverity.CRITICAL,
        affectedZones: ['critical_alerts'],
        isActive: true,
        authorId: report.authorId,
        reportId,
        sourceType: 'report',
        location: report.location,
        createdAt: now,
        updatedAt: now,
      },
      { merge: true },
    );

    await this.notifications.notifyNearbyReport('critical_alerts', reportId, report.category).catch((error) => {
      this.logger.warn(`Could not notify critical report ${reportId}: ${error?.message ?? error}`);
    });
  }

  private applyQueryFilters(reports: Report[], query: QueryReportsDto, activeOnly: boolean): Report[] {
    return reports.filter((report) => {
      if (activeOnly && !ACTIVE_REPORT_STATUSES.includes(report.status)) return false;
      if (query.status && report.status !== query.status) return false;
      if (query.category && report.category !== query.category) return false;
      return true;
    });
  }

  private applyStartAfter(reports: Report[], startAfter?: string): Report[] {
    if (!startAfter) return reports;

    const cursorIndex = reports.findIndex((report) => report.id === startAfter);
    return cursorIndex >= 0 ? reports.slice(cursorIndex + 1) : reports;
  }

  async create(dto: CreateReportDto, author: User): Promise<Report> {
    if (author.status !== UserStatus.ACTIVE) {
      throw new ForbiddenException('Tu cuenta no puede crear reportes');
    }

    const id = uuid();
    const now = admin.firestore.FieldValue.serverTimestamp();
    const historyTimestamp = admin.firestore.Timestamp.now();

    // Pre-run AI analysis when requested so the initial status reflects it.
    let analysis: any = null;
    if (dto.requestAiAnalysis) {
      analysis = await this.aiAnalysis.analyze(dto.title, dto.description, dto.category);
    }

    const explicitPriority = dto.priority;
    const aiPriority = analysis?.suggestedPriority as ReportPriority | undefined;
    const finalPriority = explicitPriority ?? aiPriority ?? ReportPriority.MEDIUM;

    // Moderator/admin-authored reports start trusted. AI-detected criticals escalate.
    const authorTrusted = author.role === UserRole.MODERATOR || author.role === UserRole.ADMIN;
    const isCriticalCategory = CRITICAL_CATEGORIES.has(dto.category);
    const isCriticalPriority = finalPriority === ReportPriority.CRITICAL;

    const initialStatus: ReportStatus =
      (isCriticalCategory && isCriticalPriority) ? ReportStatus.CRITICAL
      : authorTrusted ? ReportStatus.CONFIRMED
      : ReportStatus.PENDING;

    const initialHistory: ReportStatusHistory = {
      status: initialStatus,
      changedBy: author.id,
      timestamp: historyTimestamp.toDate(),
    };
    if (initialStatus === ReportStatus.CRITICAL) {
      initialHistory.reason = 'Categoría crítica detectada al crear';
    } else if (authorTrusted) {
      initialHistory.reason = 'Reporte verificado al origen (rol confiable)';
    }

    const data: Record<string, any> = {
      title: dto.title,
      description: dto.description,
      category: dto.category,
      status: initialStatus,
      priority: finalPriority,
      location: { lat: dto.lat, lng: dto.lng },
      address: dto.address ?? null,
      authorId: author.id,
      isAnonymous: dto.isAnonymous ?? false,
      sourceProfile: this.buildSourceProfile(author),
      media: this.sanitizeMedia(dto.media ?? []),
      confirmCount: 0,
      falseCount: 0,
      duplicateCount: 0,
      urgentCount: 0,
      resolvedSignalCount: 0,
      weightedConfirmScore: 0,
      weightedFalseScore: 0,
      weightedDuplicateScore: 0,
      weightedResolvedScore: 0,
      aiAnalysis: analysis,
      statusHistory: [initialHistory],
      createdAt: now,
      updatedAt: now,
    };

    await this.col.doc(id).set(data);
    await this.usersService.incrementReportCount(author.id);
    await this.usersService.addReputationPoints(author.id, 10);

    // Critical incidents become visible alerts and broadcast immediately.
    if (initialStatus === ReportStatus.CRITICAL) {
      await this.publishCriticalReportAlert(id, {
        title: dto.title,
        description: dto.description,
        category: dto.category,
        location: { lat: dto.lat, lng: dto.lng },
        authorId: author.id,
      });
    }

    this.logger.log(`Report created: ${id} by ${author.id} status=${initialStatus} priority=${finalPriority}`);
    return this.findById(id);
  }

  async findAll(query: QueryReportsDto): Promise<Report[]> {
    const radiusKm = query.radiusKm;
    const hasGeoFilter = query.lat !== undefined && query.lng !== undefined && radiusKm !== undefined;
    const activeOnly = query.activeOnly ?? (hasGeoFilter && !query.status);
    const requestedLimit = query.limit ?? 20;
    const fetchLimit = hasGeoFilter ? Math.max(requestedLimit * 5, 100) : requestedLimit;

    let q: admin.firestore.Query = this.col.orderBy('createdAt', 'desc');

    if (query.status) q = q.where('status', '==', query.status);
    if (!query.status && activeOnly) q = q.where('status', 'in', ACTIVE_REPORT_STATUSES);
    if (query.category) q = q.where('category', '==', query.category);

    q = q.limit(fetchLimit);

    if (query.startAfter) {
      const cursor = await this.col.doc(query.startAfter).get();
      if (cursor.exists) q = q.startAfter(cursor);
    }

    const { snapshot, usedFallback } = await getWithIndexFallback(
      q,
      this.col,
      this.logger,
      'reports.findAll',
    );

    let reports = snapshot.docs.map((d) => this.docToReport(d.id, d.data()));

    if (usedFallback) {
      reports = this.applyStartAfter(
        this.sortNewestFirst(this.applyQueryFilters(reports, query, activeOnly)),
        query.startAfter,
      ).slice(0, fetchLimit);
    }

    // Client-side geo filtering (Firestore doesn't support native geo queries without GeoFirestore)
    if (hasGeoFilter) {
      const radiusDeg = radiusKm / 111;
      reports = reports.filter((r) => {
        const dlat = Math.abs(r.location.lat - query.lat);
        const dlng = Math.abs(r.location.lng - query.lng);
        return dlat <= radiusDeg && dlng <= radiusDeg;
      });
    }

    return reports.slice(0, requestedLimit).map((report) => this.toListReport(report));
  }

  async findByUser(userId: string, limit = 20): Promise<Report[]> {
    const { snapshot } = await getWithIndexFallback(
      this.col
        .where('authorId', '==', userId)
        .orderBy('createdAt', 'desc')
        .limit(limit),
      this.col,
      this.logger,
      'reports.findByUser',
    );

    return this.sortNewestFirst(
      snapshot.docs
        .map((d) => this.docToReport(d.id, d.data()))
        .filter((report) => report.authorId === userId),
    ).slice(0, limit).map((report) => this.toListReport(report));
  }

  async findById(id: string): Promise<Report> {
    const doc = await this.col.doc(id).get();
    if (!doc.exists) throw new NotFoundException(`Report ${id} not found`);
    return this.docToReport(doc.id, doc.data());
  }

  async update(id: string, dto: UpdateReportDto, requestUser: User): Promise<Report> {
    const report = await this.findById(id);
    const canEdit = report.authorId === requestUser.id ||
      requestUser.role === UserRole.MODERATOR ||
      requestUser.role === UserRole.ADMIN;

    if (!canEdit) throw new ForbiddenException('Cannot edit this report');

    const updates: Record<string, any> = { updatedAt: admin.firestore.FieldValue.serverTimestamp() };
    if (dto.title) updates.title = dto.title;
    if (dto.description) updates.description = dto.description;
    if (dto.priority) updates.priority = dto.priority;
    if (dto.address !== undefined) updates.address = dto.address || null;

    await this.col.doc(id).update(updates);
    return this.findById(id);
  }

  async updateStatus(id: string, dto: UpdateReportStatusDto, requestUser: User): Promise<Report> {
    if (requestUser.role === UserRole.CITIZEN) {
      throw new ForbiddenException('Solo moderadores y administradores pueden cambiar el estado');
    }
    if (requestUser.status !== UserStatus.ACTIVE) {
      throw new ForbiddenException('Tu cuenta no puede moderar reportes');
    }

    const report = await this.findById(id);

    if (report.status === dto.status) {
      throw new BadRequestException('El reporte ya está en ese estado');
    }

    const allowed = STATUS_TRANSITIONS[report.status] ?? [];
    if (!allowed.includes(dto.status)) {
      throw new BadRequestException(
        `Transición inválida: ${report.status} → ${dto.status}`,
      );
    }

    const now = admin.firestore.FieldValue.serverTimestamp();
    const historyTimestamp = admin.firestore.Timestamp.now();

    const historyEntry = {
      status: dto.status,
      changedBy: requestUser.id,
      reason: dto.reason ?? null,
      timestamp: historyTimestamp,
    };

    const updates: Record<string, any> = {
      status: dto.status,
      statusHistory: admin.firestore.FieldValue.arrayUnion(historyEntry),
      updatedAt: now,
    };

    if (dto.status === ReportStatus.RESOLVED) {
      updates.resolvedAt = now;
    }
    if (dto.status === ReportStatus.CRITICAL) {
      updates.priority = ReportPriority.CRITICAL;
    }

    await this.col.doc(id).update(updates);

    // Reputation impact on author based on moderator decision
    if (report.authorId) {
      if (dto.status === ReportStatus.CONFIRMED || dto.status === ReportStatus.CRITICAL) {
        await this.usersService.addReputationPoints(report.authorId, 20);
        await this.usersService.incrementConfirmedReports(report.authorId);
      } else if (dto.status === ReportStatus.FALSE) {
        await this.usersService.addReputationPoints(report.authorId, -25);
      } else if (dto.status === ReportStatus.DUPLICATED) {
        await this.usersService.addReputationPoints(report.authorId, -5);
      } else if (dto.status === ReportStatus.RESOLVED) {
        await this.usersService.addReputationPoints(report.authorId, 5);
      }
    }

    // Notify report author
    if (!report.isAnonymous && report.authorId) {
      await this.notifications.notifyReportStatusChange(report.authorId, id, dto.status);
    }

    // Critical escalations become visible alerts and broadcast immediately.
    if (dto.status === ReportStatus.CRITICAL) {
      await this.publishCriticalReportAlert(id, report);
    }

    return this.findById(id);
  }

  async analyzeWithAi(id: string): Promise<Report> {
    const report = await this.findById(id);
    const analysis = await this.aiAnalysis.analyze(report.title, report.description, report.category);
    await this.col.doc(id).update({
      aiAnalysis: analysis,
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    });
    return this.findById(id);
  }

  async delete(id: string, requestUser: User): Promise<void> {
    const report = await this.findById(id);
    const canDelete = report.authorId === requestUser.id || requestUser.role === UserRole.ADMIN;
    if (!canDelete) throw new ForbiddenException('Cannot delete this report');
    await this.col.doc(id).delete();
  }
}
