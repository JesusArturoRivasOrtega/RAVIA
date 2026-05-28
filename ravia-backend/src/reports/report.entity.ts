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

export enum ReportPriority {
  LOW = 'low',
  MEDIUM = 'medium',
  HIGH = 'high',
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

export interface GeoPoint {
  lat: number;
  lng: number;
}

export interface ReportMedia {
  id: string;
  url: string;
  type: 'image' | 'video' | 'audio';
  thumbnailUrl?: string;
}

export interface ReportSourceProfile {
  displayName: string;
  role: string;
  reputationPoints: number;
  reportCount: number;
  confirmedReports: number;
  trustScore: number;
}

export interface ReportStatusHistory {
  status: ReportStatus;
  changedBy: string;
  reason?: string;
  timestamp: Date;
}

export interface AiAnalysisResult {
  suggestedCategory: ReportCategory;
  suggestedPriority: ReportPriority;
  confidence: number;
  summary: string;
  missingInfo: string[];
  possibleDuplicate: boolean;
  duplicateReportId?: string;
  analyzedAt: Date;
}

export interface Report {
  id: string;
  title: string;
  description: string;
  category: ReportCategory;
  status: ReportStatus;
  priority: ReportPriority;
  location: GeoPoint;
  address?: string;
  authorId: string;
  isAnonymous: boolean;
  sourceProfile?: ReportSourceProfile;
  media: ReportMedia[];
  confirmCount: number;
  falseCount: number;
  duplicateCount?: number;
  urgentCount?: number;
  resolvedSignalCount?: number;
  weightedConfirmScore?: number;
  weightedFalseScore?: number;
  weightedDuplicateScore?: number;
  weightedResolvedScore?: number;
  aiAnalysis?: AiAnalysisResult;
  statusHistory: ReportStatusHistory[];
  resolvedAt?: Date;
  createdAt: Date;
  updatedAt: Date;
}
