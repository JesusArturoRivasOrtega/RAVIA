export enum UserRole {
  CITIZEN = 'citizen',
  MODERATOR = 'moderator',
  ADMIN = 'admin',
}

export enum ReportStatus {
  PENDING = 'pending',
  VERIFYING = 'verifying',
  CONFIRMED = 'confirmed',
  IN_PROGRESS = 'in_progress',
  RESOLVED = 'resolved',
  FALSE = 'false',
  DUPLICATED = 'duplicated',
  CRITICAL = 'critical',
}

export enum ReportCategory {
  FIRE = 'fire',
  ACCIDENT = 'accident',
  FLOOD = 'flood',
  THEFT = 'theft',
  ASSAULT = 'assault',
  MISSING_PERSON = 'missing_person',
  INFRASTRUCTURE = 'infrastructure',
  GAS_LEAK = 'gas_leak',
  MEDICAL = 'medical',
  SUSPICIOUS = 'suspicious',
  OTHER = 'other',
}

export enum AlertSeverity {
  INFO = 'info',
  CAUTION = 'caution',
  URGENT = 'urgent',
  CRITICAL = 'critical',
}

export interface User {
  id: string;
  email: string;
  displayName: string;
  photoUrl?: string;
  role: UserRole;
  status: string;
  reputationPoints: number;
  reportCount: number;
  createdAt: string;
  lastLoginAt?: string;
}

export interface Report {
  id: string;
  title: string;
  description: string;
  category: ReportCategory;
  status: ReportStatus;
  priority: string;
  location: { lat: number; lng: number };
  address?: string;
  authorId: string;
  isAnonymous: boolean;
  confirmCount: number;
  falseCount: number;
  aiAnalysis?: {
    suggestedCategory: string;
    confidence: number;
    summary: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface Alert {
  id: string;
  title: string;
  description: string;
  severity: AlertSeverity;
  affectedZones: string[];
  isActive: boolean;
  authorId: string;
  createdAt: string;
}

export interface RiskZone {
  id: string;
  name: string;
  description: string;
  riskLevel: string;
  centerLat: number;
  centerLng: number;
  radiusMeters: number;
  reportCount: number;
  isActive: boolean;
  createdAt: string;
}

export interface MissingPerson {
  id: string;
  name: string;
  age?: number;
  lastSeenLocation: string;
  description: string;
  contactInfo: string;
  status: string;
  sightings: Array<{ id: string; comment?: string; createdAt: string }>;
  createdAt: string;
}

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
