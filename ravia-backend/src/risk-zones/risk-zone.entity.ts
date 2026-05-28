export enum RiskLevel {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
  CRITICAL = 'critical',
}

export interface RiskZone {
  id: string;
  name: string;
  description: string;
  riskLevel: RiskLevel;
  centerLat: number;
  centerLng: number;
  radiusMeters: number;
  reportCount: number;
  isActive: boolean;
  authorId: string;
  createdAt: Date;
  updatedAt: Date;
}
