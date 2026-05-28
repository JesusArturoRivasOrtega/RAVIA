export enum AlertSeverity {
  INFO = 'info',
  CAUTION = 'caution',
  URGENT = 'urgent',
  CRITICAL = 'critical',
}

export interface Alert {
  id: string;
  title: string;
  description: string;
  severity: AlertSeverity;
  affectedZones: string[];
  isActive: boolean;
  authorId: string;
  expiresAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}
