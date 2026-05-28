import { cn, STATUS_COLORS, STATUS_LABELS, SEVERITY_COLORS } from '@/lib/utils';
import { ReportStatus, AlertSeverity } from '@/types';

export function StatusBadge({ status }: { status: ReportStatus }) {
  const colors = STATUS_COLORS[status] ?? STATUS_COLORS[ReportStatus.PENDING];
  return (
    <span className={cn('badge ring-1 ring-inset ring-black/5', colors.bg, colors.text)}>
      <span className={cn('h-1.5 w-1.5 rounded-full', colors.dot)} />
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}

export function SeverityBadge({ severity }: { severity: AlertSeverity }) {
  const colors = SEVERITY_COLORS[severity] ?? SEVERITY_COLORS[AlertSeverity.INFO];
  const labels: Record<AlertSeverity, string> = {
    [AlertSeverity.INFO]: 'Informativo',
    [AlertSeverity.CAUTION]: 'Precaución',
    [AlertSeverity.URGENT]: 'Urgente',
    [AlertSeverity.CRITICAL]: 'Crítico',
  };
  return (
    <span className={cn('badge ring-1 ring-inset ring-black/5', colors.bg, colors.text)}>
      {labels[severity] ?? severity}
    </span>
  );
}
