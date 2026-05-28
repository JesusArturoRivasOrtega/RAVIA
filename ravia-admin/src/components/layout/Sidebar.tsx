'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LogOut } from 'lucide-react';
import { RaviaGlyph, type RaviaGlyphName } from '@/components/brand/RaviaGlyph';
import { RaviaMark } from '@/components/brand/RaviaMark';
import { cn } from '@/lib/utils';
import { useAuthStore } from '@/lib/auth-store';
import { signOut } from 'firebase/auth';
import { auth } from '@/lib/firebase';

const NAV_ITEMS: Array<{ href: string; label: string; glyph: RaviaGlyphName }> = [
  { href: '/dashboard', label: 'Dashboard', glyph: 'dashboard' },
  { href: '/reports', label: 'Reportes', glyph: 'reports' },
  { href: '/alerts', label: 'Alertas', glyph: 'alerts' },
  { href: '/risk-zones', label: 'Zonas de riesgo', glyph: 'zones' },
  { href: '/missing-persons', label: 'Desaparecidos', glyph: 'missing' },
  { href: '/users', label: 'Usuarios', glyph: 'users' },
  { href: '/statistics', label: 'Estadísticas', glyph: 'statistics' },
];

export function Sidebar() {
  const pathname = usePathname();
  const { user, setUser } = useAuthStore();

  const handleLogout = async () => {
    if (auth) {
      await signOut(auth);
    }
    setUser(null);
  };

  return (
    <aside className="sticky top-0 flex h-screen w-64 min-w-[16rem] flex-col border-r border-white/10 bg-[#0A1628] text-white">
      <div className="p-5">
        <div className="flex items-center gap-3">
          <RaviaMark className="h-11 w-11" />
          <div>
            <p className="text-lg font-bold leading-none tracking-normal">RAVIA</p>
            <p className="mt-1 text-xs text-white/55">Centro de comando</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 space-y-1 px-3 py-2">
        {NAV_ITEMS.map(({ href, label, glyph }) => {
          const isActive = pathname === href || pathname.startsWith(`${href}/`);
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                'group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200',
                isActive
                  ? 'bg-white text-navy-900 shadow-sm'
                  : 'text-white/68 hover:bg-white/10 hover:text-white',
              )}
            >
              <span
                className={cn(
                  'grid h-8 w-8 place-items-center rounded-md transition-colors',
                  isActive ? 'bg-navy-50 text-navy-700' : 'bg-white/10 text-white/70 group-hover:text-white',
                )}
              >
                <RaviaGlyph name={glyph} className="h-[18px] w-[18px]" />
              </span>
              <span className="truncate">{label}</span>
            </Link>
          );
        })}
      </nav>

      <div className="p-4">
        {user && (
          <div className="mb-3 rounded-lg border border-white/10 bg-white/[0.06] p-3">
            <div className="flex items-center gap-3">
              <div className="grid h-9 w-9 place-items-center rounded-lg bg-white/12 text-sm font-semibold">
                {user.displayName?.[0]?.toUpperCase() ?? '?'}
              </div>
              <div className="min-w-0">
                <p className="truncate text-sm font-medium">{user.displayName}</p>
                <p className="text-xs capitalize text-white/50">{user.role}</p>
              </div>
            </div>
          </div>
        )}
        <button
          onClick={handleLogout}
          className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm text-white/65 transition-colors hover:bg-white/10 hover:text-white"
        >
          <LogOut className="h-4 w-4" />
          Cerrar sesión
        </button>
      </div>
    </aside>
  );
}
