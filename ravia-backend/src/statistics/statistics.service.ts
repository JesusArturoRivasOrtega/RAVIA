import { Injectable } from '@nestjs/common';
import { FirebaseService } from '../firebase/firebase.service';
import { ReportStatus, ReportCategory } from '../reports/report.entity';

export interface DashboardStats {
  totalReports: number;
  activeReports: number;
  resolvedReports: number;
  totalUsers: number;
  activeAlerts: number;
  activeMissingPersons: number;
  reportsByCategory: Record<string, number>;
  reportsByStatus: Record<string, number>;
  recentActivity: number;
}

@Injectable()
export class StatisticsService {
  constructor(private readonly firebase: FirebaseService) {}

  async getDashboard(): Promise<DashboardStats> {
    const [reportsSnap, usersSnap, alertsSnap, missingSnap] = await Promise.all([
      this.firebase.collection('reports').get(),
      this.firebase.collection('users').get(),
      this.firebase.collection('alerts').where('isActive', '==', true).get(),
      this.firebase.collection('missing_persons').where('status', '==', 'active').get(),
    ]);

    const reports = reportsSnap.docs.map((d) => d.data());

    const reportsByStatus: Record<string, number> = {};
    const reportsByCategory: Record<string, number> = {};

    for (const r of reports) {
      reportsByStatus[r.status] = (reportsByStatus[r.status] ?? 0) + 1;
      reportsByCategory[r.category] = (reportsByCategory[r.category] ?? 0) + 1;
    }

    const activeStatuses = [ReportStatus.PENDING, ReportStatus.VERIFYING, ReportStatus.CONFIRMED, ReportStatus.IN_PROGRESS, ReportStatus.CRITICAL];
    const activeReports = reports.filter((r) => activeStatuses.includes(r.status)).length;
    const resolvedReports = reports.filter((r) => r.status === ReportStatus.RESOLVED).length;

    // Recent activity = reports in last 24 hours
    const oneDayAgo = new Date(Date.now() - 86400000);
    const recentActivity = reports.filter((r) => {
      const createdAt = r.createdAt?.toDate?.() ?? new Date(0);
      return createdAt > oneDayAgo;
    }).length;

    return {
      totalReports: reports.length,
      activeReports,
      resolvedReports,
      totalUsers: usersSnap.size,
      activeAlerts: alertsSnap.size,
      activeMissingPersons: missingSnap.size,
      reportsByCategory,
      reportsByStatus,
      recentActivity,
    };
  }

  async getReportTrends(days = 7): Promise<Array<{ date: string; count: number }>> {
    const since = new Date();
    since.setDate(since.getDate() - days);

    const snap = await this.firebase.collection('reports')
      .where('createdAt', '>=', since)
      .orderBy('createdAt', 'asc')
      .get();

    const byDay: Record<string, number> = {};
    snap.docs.forEach((d) => {
      const date = d.data().createdAt?.toDate?.()?.toISOString()?.slice(0, 10);
      if (date) byDay[date] = (byDay[date] ?? 0) + 1;
    });

    return Object.entries(byDay).map(([date, count]) => ({ date, count }));
  }
}
