import { cn } from '@/lib/utils';

export type RaviaGlyphName =
  | 'dashboard'
  | 'reports'
  | 'alerts'
  | 'zones'
  | 'missing'
  | 'users'
  | 'statistics'
  | 'resolved'
  | 'activity'
  | 'shield';

interface RaviaGlyphProps {
  name: RaviaGlyphName;
  className?: string;
}

const paths: Record<RaviaGlyphName, string[]> = {
  dashboard: ['M4 13h7V4H4v9Z', 'M13 20h7V4h-7v16Z', 'M4 20h7v-5H4v5Z'],
  reports: ['M7 4h8l4 4v12H7V4Z', 'M14 4v5h5', 'M10 13h6', 'M10 17h4'],
  alerts: ['M12 4 4.5 18h15L12 4Z', 'M12 9v4', 'M12 16h.01'],
  zones: ['M12 3 5 6v6c0 4.4 2.9 7.2 7 9 4.1-1.8 7-4.6 7-9V6l-7-3Z', 'M9 12h6', 'M12 9v6'],
  missing: ['M10 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z', 'M3.5 20a6.5 6.5 0 0 1 9.7-5.7', 'M17 17l3 3', 'M20 17l-3 3'],
  users: ['M8 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Z', 'M3 20a5 5 0 0 1 10 0', 'M17 11a3 3 0 1 0 0-6', 'M15 15a4.8 4.8 0 0 1 6 4.5'],
  statistics: ['M5 19V9', 'M12 19V5', 'M19 19v-7', 'M4 19h16'],
  resolved: ['M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z', 'm8.5 12 2.4 2.4 4.8-5.2'],
  activity: ['M4 13h4l2-7 4 12 2-5h4'],
  shield: ['M12 3 5 6v6c0 4.4 2.9 7.2 7 9 4.1-1.8 7-4.6 7-9V6l-7-3Z', 'M9.5 12.5 11.3 14l3.7-4'],
};

export function RaviaGlyph({ name, className }: RaviaGlyphProps) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      aria-hidden="true"
      className={cn('h-5 w-5', className)}
    >
      {paths[name].map((path, index) => (
        <path
          key={index}
          d={path}
          stroke="currentColor"
          strokeWidth="1.8"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      ))}
    </svg>
  );
}
