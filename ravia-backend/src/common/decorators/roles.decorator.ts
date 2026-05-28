import { SetMetadata } from '@nestjs/common';

export enum UserRole {
  CITIZEN = 'citizen',
  MODERATOR = 'moderator',
  ADMIN = 'admin',
}

export const ROLES_KEY = 'roles';
export const Roles = (...roles: UserRole[]) => SetMetadata(ROLES_KEY, roles);
