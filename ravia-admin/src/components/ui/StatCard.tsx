import { RaviaGlyph, type RaviaGlyphName } from '@/components/brand/RaviaGlyph';
import { cn } from '@/lib/utils';

interface StatCardProps {
  title: string;
  value: number | string;
  glyph: RaviaGlyphName;
  color?: 'blue' | 'green' | 'red' | 'amber' | 'purple' | 'cyan';
  subtitle?: string;
}

const COLOR_MAP = {
  blue: { bg: 'bg-blue-50', icon: 'text-blue-700', border: 'border-blue-100', glow: 'from-blue-500/10' },
  green: { bg: 'bg-green-50', icon: 'text-green-700', border: 'border-green-100', glow: 'from-green-500/10' },
  red: { bg: 'bg-red-50', icon: 'text-red-700', border: 'border-red-100', glow: 'from-red-500/10' },
  amber: { bg: 'bg-amber-50', icon: 'text-amber-700', border: 'border-amber-100', glow: 'from-amber-500/10' },
  purple: { bg: 'bg-purple-50', icon: 'text-purple-700', border: 'border-purple-100', glow: 'from-purple-500/10' },
  cyan: { bg: 'bg-cyan-50', icon: 'text-cyan-700', border: 'border-cyan-100', glow: 'from-cyan-500/10' },
};

export function StatCard({ title, value, glyph, color = 'blue', subtitle }: StatCardProps) {
  const c = COLOR_MAP[color];
  return (
    <div className={cn('card interactive-card panel-enter relative overflow-hidden p-5 border', c.border)}>
      <div className={cn('pointer-events-none absolute inset-x-0 top-0 h-24 bg-gradient-to-b to-transparent', c.glow)} />
      <div className="relative flex items-start gap-4">
        <div className={cn('grid h-10 w-10 place-items-center rounded-lg ring-1 ring-inset', c.bg, c.icon, c.border)}>
          <RaviaGlyph name={glyph} className="h-5 w-5" />
        </div>
        <div className="min-w-0">
          <p className="text-2xl font-bold leading-none text-slate-950">{value}</p>
          <p className="mt-1 text-sm font-medium text-slate-600">{title}</p>
          {subtitle && <p className="mt-0.5 text-xs text-slate-400">{subtitle}</p>}
        </div>
      </div>
    </div>
  );
}
