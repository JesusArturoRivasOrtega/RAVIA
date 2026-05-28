export enum MissingPersonStatus {
  PENDING_REVIEW = 'pending_review',
  ACTIVE = 'active',
  FOUND = 'found',
  CANCELLED = 'cancelled',
}

export interface Sighting {
  id: string;
  reportedBy: string;
  lat: number;
  lng: number;
  comment?: string;
  photoUrl?: string;
  createdAt: Date;
}

export interface MissingPerson {
  id: string;
  name: string;
  age?: number;
  photoUrl?: string;
  lastSeenLocation: string;
  lastSeenLat?: number;
  lastSeenLng?: number;
  clothing?: string;
  distinctiveSigns?: string;
  description: string;
  contactInfo: string;
  status: MissingPersonStatus;
  reportedBy: string;
  sightings: Sighting[];
  createdAt: Date;
  updatedAt: Date;
}
