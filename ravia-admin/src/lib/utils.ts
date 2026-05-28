import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { formatDistanceToNow, format } from 'date-fns';
import { es } from 'date-fns/locale';
import { ReportStatus, AlertSeverity } from '@/types';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function relativeTime(date: string): string {
  return formatDistanceToNow(new Date(date), { addSuffix: true, locale: es });
}

export function formatDate(date: string): string {
  return format(new Date(date), 'dd/MM/yyyy HH:mm', { locale: es });
}

export const STATUS_COLORS: Record<ReportStatus, { bg: string; text: string; dot: string }> = {
  [ReportStatus.PENDING]: { bg: 'bg-gray-100', text: 'text-gray-600', dot: 'bg-gray-400' },
  [ReportStatus.VERIFYING]: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-400' },
  [ReportStatus.CONFIRMED]: { bg: 'bg-green-100', text: 'text-green-700', dot: 'bg-green-500' },
  [ReportStatus.IN_PROGRESS]: { bg: 'bg-blue-100', text: 'text-blue-700', dot: 'bg-blue-500' },
  [ReportStatus.RESOLVED]: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-600' },
  [ReportStatus.FALSE]: { bg: 'bg-gray-100', text: 'text-gray-500', dot: 'bg-gray-400' },
  [ReportStatus.DUPLICATED]: { bg: 'bg-purple-100', text: 'text-purple-700', dot: 'bg-purple-500' },
  [ReportStatus.CRITICAL]: { bg: 'bg-red-100', text: 'text-red-700', dot: 'bg-red-500' },
};

export const STATUS_LABELS: Record<ReportStatus, string> = {
  [ReportStatus.PENDING]: 'Pendiente',
  [ReportStatus.VERIFYING]: 'Verificando',
  [ReportStatus.CONFIRMED]: 'Confirmado',
  [ReportStatus.IN_PROGRESS]: 'En proceso',
  [ReportStatus.RESOLVED]: 'Resuelto',
  [ReportStatus.FALSE]: 'Falso',
  [ReportStatus.DUPLICATED]: 'Duplicado',
  [ReportStatus.CRITICAL]: 'Crítico',
};

export const SEVERITY_COLORS: Record<AlertSeverity, { bg: string; text: string }> = {
  [AlertSeverity.INFO]: { bg: 'bg-blue-100', text: 'text-blue-700' },
  [AlertSeverity.CAUTION]: { bg: 'bg-amber-100', text: 'text-amber-700' },
  [AlertSeverity.URGENT]: { bg: 'bg-orange-100', text: 'text-orange-700' },
  [AlertSeverity.CRITICAL]: { bg: 'bg-red-100', text: 'text-red-700' },
};

export const CATEGORY_LABELS: Record<string, string> = {
  fire: 'Incendio',
  accident: 'Accidente',
  flood: 'Inundación',
  theft: 'Robo',
  assault: 'Agresión',
  missing_person: 'Desaparecido',
  infrastructure: 'Infraestructura',
  gas_leak: 'Fuga de gas',
  medical: 'Médico',
  suspicious: 'Sospechoso',
  other: 'Otro',
};

export const TERMINAL_REPORT_STATUSES = new Set<ReportStatus>([
  ReportStatus.RESOLVED,
  ReportStatus.FALSE,
  ReportStatus.DUPLICATED,
]);

export function allowedReportStatusTransitions(status: ReportStatus): ReportStatus[] {
  switch (status) {
    case ReportStatus.PENDING:
      return [
        ReportStatus.VERIFYING,
        ReportStatus.CONFIRMED,
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED,
      ];
    case ReportStatus.VERIFYING:
      return [
        ReportStatus.CONFIRMED,
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED,
      ];
    case ReportStatus.CONFIRMED:
      return [
        ReportStatus.CRITICAL,
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED,
      ];
    case ReportStatus.CRITICAL:
      return [
        ReportStatus.IN_PROGRESS,
        ReportStatus.RESOLVED,
        ReportStatus.FALSE,
        ReportStatus.DUPLICATED,
      ];
    case ReportStatus.IN_PROGRESS:
      return [ReportStatus.RESOLVED, ReportStatus.FALSE, ReportStatus.DUPLICATED];
    default:
      return [];
  }
}
