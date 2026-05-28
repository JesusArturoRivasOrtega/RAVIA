import {
  Injectable,
  NotFoundException,
  ConflictException,
  ForbiddenException,
  BadRequestException,
  Logger,
} from '@nestjs/common';
import * as admin from 'firebase-admin';
import { v4 as uuid } from 'uuid';
import { FirebaseService } from '../firebase/firebase.service';
import { UsersService } from '../users/users.service';
import { NotificationsService } from '../notifications/notifications.service';
import { User, UserRole, UserStatus } from '../users/user.entity';
import {
  ReportCategory,
  ReportPriority,
  ReportStatus,
} from '../reports/report.entity';

export enum ConfirmationType {
  CONFIRM = 'confirm',
  FALSE_REPORT = 'false_report',
  DUPLICATE = 'duplicate',
  URGENT = 'urgent',
  NO_LONGER_HAPPENING = 'no_longer_happening',
  MORE_INFO = 'more_info',
}

export interface ReportConfirmation {
  id: string;
  reportId: string;
  userId: string;
  type: ConfirmationType;
  comment?: string;
  weight: number;
  createdAt: Date;
}

const COLLECTION = 'report_confirmations';
const REPORTS_COLLECTION = 'reports';

// Categories that auto-escalate when community confirms
const CRITICAL_CATEGORIES = new Set<ReportCategory>([
  ReportCategory.FIRE,
  ReportCategory.GAS_LEAK,
  ReportCategory.MEDICAL,
  ReportCategory.MISSING_PERSON,
]);

// Verification thresholds (weighted)
const VERIFY_THRESHOLD = 3.0; // weighted confirms to mark CONFIRMED
const FALSE_THRESHOLD = 3.0;  // weighted false reports to mark FALSE
const DUPLICATE_THRESHOLD = 2.0;
const RESOLVED_THRESHOLD = 3.0;

@Injectable()
export class ReportConfirmationsService {
  private readonly logger = new Logger(ReportConfirmationsService.name);

  constructor(
    private readonly firebase: FirebaseService,
    private readonly usersService: UsersService,
    private readonly notifications: NotificationsService,
  ) {}

  private get col() {
    return this.firebase.collection(COLLECTION);
  }

  /** Trust weight by role + reputation bonus, capped. */
  private voteWeight(user: User): number {
    const roleWeight =
      user.role === UserRole.ADMIN ? 5
      : user.role === UserRole.MODERATOR ? 3
      : 1;
    const reputationBonus = Math.min(2, Math.max(0, user.reputationPoints) / 100 * 0.5);
    return Number((roleWeight + reputationBonus).toFixed(2));
  }

  /** Increment or noop based on type. */
  private incrementsFor(type: ConfirmationType, weight: number): Record<string, admin.firestore.FieldValue> {
    const inc = admin.firestore.FieldValue.increment;
    switch (type) {
      case ConfirmationType.CONFIRM:
      case ConfirmationType.URGENT:
      case ConfirmationType.MORE_INFO:
        return {
          confirmCount: inc(1),
          weightedConfirmScore: inc(weight),
          ...(type === ConfirmationType.URGENT && { urgentCount: inc(1) }),
        };
      case ConfirmationType.FALSE_REPORT:
        return { falseCount: inc(1), weightedFalseScore: inc(weight) };
      case ConfirmationType.DUPLICATE:
        return { duplicateCount: inc(1), weightedDuplicateScore: inc(weight) };
      case ConfirmationType.NO_LONGER_HAPPENING:
        return { resolvedSignalCount: inc(1), weightedResolvedScore: inc(weight) };
      default:
        return {};
    }
  }

  async confirm(
    reportId: string,
    type: ConfirmationType,
    user: User,
    comment?: string,
  ): Promise<ReportConfirmation> {
    if (user.status !== UserStatus.ACTIVE) {
      throw new ForbiddenException('Tu cuenta está suspendida o bloqueada');
    }

    const reportRef = this.firebase.collection(REPORTS_COLLECTION).doc(reportId);
    const weight = this.voteWeight(user);
    const id = uuid();
    const now = admin.firestore.Timestamp.now();

    // Atomic transaction: read report + check duplicate + write confirmation + update counters
    const txResult = await this.firebase.firestore.runTransaction(async (tx) => {
      const reportSnap = await tx.get(reportRef);
      if (!reportSnap.exists) throw new NotFoundException(`Report ${reportId} not found`);

      const report = reportSnap.data() as Record<string, any>;

      if (report.authorId === user.id) {
        throw new ForbiddenException('No puedes validar tu propio reporte');
      }

      const terminalStates: ReportStatus[] = [
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED,
      ];
      if (terminalStates.includes(report.status)) {
        throw new BadRequestException('Este reporte ya está cerrado');
      }

      // Check duplicate confirmation by same user (idempotent)
      const existingQ = await tx.get(
        this.col
          .where('reportId', '==', reportId)
          .where('userId', '==', user.id)
          .limit(1),
      );
      if (!existingQ.empty) {
        throw new ConflictException('Ya validaste este reporte');
      }

      const confirmationRef = this.col.doc(id);
      tx.set(confirmationRef, {
        reportId,
        userId: user.id,
        userRole: user.role,
        type,
        weight,
        comment: comment ?? null,
        createdAt: now,
      });

      const counters = this.incrementsFor(type, weight);
      const updates: Record<string, any> = {
        ...counters,
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
      };

      // First confirmation moves status to VERIFYING
      if (report.status === ReportStatus.PENDING) {
        updates.status = ReportStatus.VERIFYING;
        updates.statusHistory = admin.firestore.FieldValue.arrayUnion({
          status: ReportStatus.VERIFYING,
          changedBy: 'system',
          reason: 'Primera validación comunitaria recibida',
          timestamp: now,
        });
      }

      // URGENT vote escalates priority
      if (type === ConfirmationType.URGENT && report.priority !== ReportPriority.CRITICAL) {
        updates.priority = ReportPriority.HIGH;
      }

      tx.update(reportRef, updates);

      return {
        report: { ...report, ...updates, id: reportSnap.id },
        weight,
      };
    });

    // Confirmer reputation: +5 for confirming (resolved at moderator-confirmed time too)
    if (type === ConfirmationType.CONFIRM || type === ConfirmationType.URGENT) {
      await this.usersService.addReputationPoints(user.id, 5);
    }

    // After tx — evaluate auto-transitions outside the transaction
    await this.evaluateAutoTransition(reportId);

    this.logger.log(
      `Confirmation ${type} on report ${reportId} by ${user.id} (weight=${weight})`,
    );

    return {
      id,
      reportId,
      userId: user.id,
      type,
      comment,
      weight,
      createdAt: now.toDate(),
    };
  }

  /** Evaluate community thresholds and transition report status accordingly. */
  private async evaluateAutoTransition(reportId: string): Promise<void> {
    const reportRef = this.firebase.collection(REPORTS_COLLECTION).doc(reportId);
    const snap = await reportRef.get();
    if (!snap.exists) return;
    const r = snap.data() as Record<string, any>;

    if (
      r.status === ReportStatus.RESOLVED ||
      r.status === ReportStatus.FALSE ||
      r.status === ReportStatus.DUPLICATED
    ) return;

    const confirms = Number(r.weightedConfirmScore ?? 0);
    const falses = Number(r.weightedFalseScore ?? 0);
    const dupes = Number(r.weightedDuplicateScore ?? 0);
    const resolved = Number(r.weightedResolvedScore ?? 0);
    const urgent = Number(r.urgentCount ?? 0);

    const now = admin.firestore.Timestamp.now();
    let nextStatus: ReportStatus | null = null;
    let reason = '';

    if (resolved >= RESOLVED_THRESHOLD && resolved >= Math.max(1, confirms * 0.5)) {
      nextStatus = ReportStatus.RESOLVED;
      reason = 'La comunidad indico que ya no esta ocurriendo';
    } else if (dupes >= DUPLICATE_THRESHOLD && dupes > confirms) {
      nextStatus = ReportStatus.DUPLICATED;
      reason = 'Múltiples vecinos lo marcaron como duplicado';
    } else if (falses >= FALSE_THRESHOLD && falses > confirms * 2) {
      nextStatus = ReportStatus.FALSE;
      reason = 'Comunidad lo marcó como falso';
    } else if (confirms >= VERIFY_THRESHOLD && confirms > falses * 2) {
      const cat = r.category as ReportCategory;
      const escalateCritical = CRITICAL_CATEGORIES.has(cat) || urgent >= 2;
      nextStatus = escalateCritical ? ReportStatus.CRITICAL : ReportStatus.CONFIRMED;
      reason = escalateCritical
        ? 'Confirmado por la comunidad — incidente crítico'
        : 'Confirmado por la comunidad';
    }

    if (!nextStatus || nextStatus === r.status) return;

    const updates: Record<string, any> = {
      status: nextStatus,
      statusHistory: admin.firestore.FieldValue.arrayUnion({
        status: nextStatus,
        changedBy: 'system',
        reason,
        timestamp: now,
      }),
      updatedAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    // Critical reports raise priority too
    if (nextStatus === ReportStatus.CRITICAL) {
      updates.priority = ReportPriority.CRITICAL;
    }
    if (nextStatus === ReportStatus.RESOLVED) {
      updates.resolvedAt = admin.firestore.FieldValue.serverTimestamp();
    }

    await reportRef.update(updates);

    // Reward author + push notification on confirmation
    if (nextStatus === ReportStatus.CONFIRMED || nextStatus === ReportStatus.CRITICAL) {
      await this.usersService.addReputationPoints(r.authorId, 20);
      await this.usersService.incrementConfirmedReports(r.authorId);
    } else if (nextStatus === ReportStatus.FALSE) {
      await this.usersService.addReputationPoints(r.authorId, -15);
    } else if (nextStatus === ReportStatus.DUPLICATED) {
      await this.usersService.addReputationPoints(r.authorId, -5);
    } else if (nextStatus === ReportStatus.RESOLVED) {
      await this.usersService.addReputationPoints(r.authorId, 5);
    }

    if (!r.isAnonymous && r.authorId) {
      await this.notifications.notifyReportStatusChange(r.authorId, reportId, nextStatus);
    }

    // Broadcast critical to nearby topic
    if (nextStatus === ReportStatus.CRITICAL) {
      await this.notifications.notifyNearbyReport('critical_alerts', reportId, r.category);
    }

    this.logger.log(`Auto-transition: report ${reportId} → ${nextStatus} (${reason})`);
  }

  async getForReport(reportId: string): Promise<ReportConfirmation[]> {
    const snap = await this.col
      .where('reportId', '==', reportId)
      .orderBy('createdAt', 'desc')
      .get()
      .catch(() => this.col.where('reportId', '==', reportId).get());

    return snap.docs.map((d) => {
      const data = d.data();
      return {
        id: d.id,
        reportId: data.reportId,
        userId: data.userId,
        type: data.type,
        comment: data.comment ?? undefined,
        weight: Number(data.weight ?? 1),
        createdAt: data.createdAt?.toDate?.() ?? new Date(),
      } as ReportConfirmation;
    });
  }
}
