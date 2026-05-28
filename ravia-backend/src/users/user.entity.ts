export enum UserRole {
  CITIZEN = 'citizen',
  MODERATOR = 'moderator',
  ADMIN = 'admin',
}

export enum UserStatus {
  ACTIVE = 'active',
  SUSPENDED = 'suspended',
  BANNED = 'banned',
}

export interface UserLocation {
  lat: number;
  lng: number;
  zone?: string;
}

export interface User {
  id: string;           // Firebase UID
  email: string;
  displayName: string;
  photoUrl?: string;
  role: UserRole;
  status: UserStatus;
  reputationPoints: number;
  reportCount: number;
  confirmedReports: number;
  zone?: string;
  location?: UserLocation;
  fcmTokens: string[];
  createdAt: Date;
  updatedAt: Date;
  lastLoginAt?: Date;
}
