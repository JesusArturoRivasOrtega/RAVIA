import { Injectable, Logger } from '@nestjs/common';
import { FirebaseService } from '../firebase/firebase.service';
import * as admin from 'firebase-admin';

@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(private readonly firebase: FirebaseService) {}

  async sendToTokens(tokens: string[], title: string, body: string, data?: Record<string, string>): Promise<void> {
    if (!tokens?.length) return;

    const message: admin.messaging.MulticastMessage = {
      tokens,
      notification: { title, body },
      data: { title, message: body, ...(data ?? {}) },
      android: {
        priority: 'high',
        notification: { channelId: 'ravia_alerts', sound: 'default' },
      },
    };

    try {
      const response = await this.firebase.messaging.sendEachForMulticast(message);
      this.logger.log(`Notifications sent: ${response.successCount} ok, ${response.failureCount} failed`);
    } catch (error) {
      this.logger.error('Failed to send push notifications', error);
    }
  }

  async sendToTopic(topic: string, title: string, body: string, data?: Record<string, string>): Promise<void> {
    const message: admin.messaging.Message = {
      topic,
      notification: { title, body },
      data: { title, message: body, ...(data ?? {}) },
      android: {
        priority: 'high',
        notification: { channelId: 'ravia_alerts', sound: 'default' },
      },
    };

    try {
      await this.firebase.messaging.send(message);
      this.logger.log(`Notification sent to topic: ${topic}`);
    } catch (error) {
      this.logger.error(`Failed to send to topic ${topic}`, error);
    }
  }

  async notifyReportStatusChange(userId: string, reportId: string, newStatus: string): Promise<void> {
    const userDoc = await this.firebase.collection('users').doc(userId).get();
    if (!userDoc.exists) return;

    const tokens: string[] = userDoc.data()?.fcmTokens ?? [];
    if (!tokens.length) return;

    const statusLabels: Record<string, string> = {
      pending: 'recibido',
      verifying: 'siendo verificado por vecinos',
      confirmed: 'confirmado',
      critical: 'marcado como CRÍTICO',
      in_progress: 'atendido por autoridades',
      resolved: 'resuelto',
      false: 'marcado como falso',
      duplicated: 'marcado como duplicado',
    };

    const label = statusLabels[newStatus] ?? newStatus;
    await this.sendToTokens(tokens, 'RAVIA - Actualizacion de reporte', `Tu reporte fue ${label}`, {
      type: 'report_status',
      reportId,
      status: newStatus,
    });
  }

  async broadcastCriticalAlert(title: string, description: string, alertId: string): Promise<void> {
    await this.sendToTopic('critical_alerts', `ALERTA CRITICA: ${title}`, description, {
      type: 'critical_alert',
      alertId,
    });
  }

  async notifyNearbyReport(topic: string, reportId: string, category: string): Promise<void> {
    await this.sendToTopic(topic, 'RAVIA - Reporte critico en tu zona', `Nuevo reporte de ${category} cerca de ti`, {
      type: 'nearby_report',
      reportId,
      category,
    });
  }
}
