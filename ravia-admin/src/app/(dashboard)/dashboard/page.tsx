'use client';

import { useQuery } from '@tanstack/react-query';
import { StatCard } from '@/components/ui/StatCard';
import { statisticsApi } from '@/lib/api';
import { CATEGORY_LABELS } from '@/lib/utils';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const CATEGORY_COLORS = [
  '#1B3A6B',
  '#00B4D8',
  '#2DC653',
  '#FFC107',
  '#E53935',
  '#8E24AA',
  '#1565C0',
  '#FF6D00',
  '#0097A7',
  '#78909C',
  '#334155',
];

export default function DashboardPage() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard'],
    queryFn: statisticsApi.dashboard,
    refetchInterval: 30000,
  });

  const { data: trends } = useQuery({
    queryKey: ['trends'],
    queryFn: () => statisticsApi.trends(7),
    refetchInterval: 60000,
  });

  if (isLoading || !stats) {
    return (
      <div className="grid min-h-96 place-items-center p-8">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-navy-700 border-t-transparent" />
      </div>
    );
  }

  const categoryData = Object.entries(stats.reportsByCategory ?? {}).map(([key, value]) => ({
    name: CATEGORY_LABELS[key] ?? key,
    value: value as number,
  }));

  const statusData = Object.entries(stats.reportsByStatus ?? {}).map(([key, value]) => ({
    name: key,
    value: value as number,
  }));

  return (
    <div className="space-y-6 p-6 lg:p-8">
      <div className="panel-enter flex flex-col gap-3 rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-normal text-slate-950">Dashboard</h1>
          <p className="mt-1 text-sm text-slate-500">Resumen operativo de la plataforma RAVIA</p>
        </div>
        <div className="flex items-center gap-2 rounded-full bg-green-50 px-3 py-1 text-xs font-medium text-green-700">
          <span className="h-2 w-2 animate-pulse rounded-full bg-green-500" />
          Sincronización activa
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard title="Total reportes" value={stats.totalReports} glyph="reports" color="blue" />
        <StatCard title="Reportes activos" value={stats.activeReports} glyph="alerts" color="amber" />
        <StatCard title="Resueltos" value={stats.resolvedReports} glyph="resolved" color="green" />
        <StatCard title="Total usuarios" value={stats.totalUsers} glyph="users" color="cyan" />
        <StatCard title="Alertas activas" value={stats.activeAlerts} glyph="alerts" color="red" />
        <StatCard title="Desaparecidos" value={stats.activeMissingPersons} glyph="missing" color="purple" />
        <StatCard title="Actividad 24h" value={stats.recentActivity} glyph="activity" color="green" subtitle="nuevos reportes" />
        <StatCard title="Por resolver" value={stats.activeReports} glyph="dashboard" color="amber" subtitle="requieren atención" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="card interactive-card panel-enter p-6">
          <h3 className="mb-4 text-base font-semibold text-slate-950">Reportes últimos 7 días</h3>
          {trends && trends.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={trends}>
                <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#1B3A6B" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex h-52 items-center justify-center text-sm text-slate-400">Sin datos de tendencias</div>
          )}
        </div>

        <div className="card interactive-card panel-enter p-6">
          <h3 className="mb-4 text-base font-semibold text-slate-950">Reportes por categoría</h3>
          {categoryData.length > 0 ? (
            <div className="flex items-center gap-4">
              <ResponsiveContainer width="60%" height={220}>
                <PieChart>
                  <Pie data={categoryData} cx="50%" cy="50%" innerRadius={54} outerRadius={84} paddingAngle={3} dataKey="value">
                    {categoryData.map((_, i) => (
                      <Cell key={i} fill={CATEGORY_COLORS[i % CATEGORY_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="min-w-0 flex-1 space-y-1.5 text-xs">
                {categoryData.slice(0, 6).map((item, i) => (
                  <div key={item.name} className="flex items-center gap-2">
                    <span
                      className="h-2.5 w-2.5 flex-shrink-0 rounded-full"
                      style={{ backgroundColor: CATEGORY_COLORS[i % CATEGORY_COLORS.length] }}
                    />
                    <span className="truncate text-slate-600">{item.name}</span>
                    <span className="ml-auto font-medium text-slate-950">{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="flex h-52 items-center justify-center text-sm text-slate-400">Sin datos</div>
          )}
        </div>
      </div>

      <div className="card panel-enter p-6">
        <h3 className="mb-4 text-base font-semibold text-slate-950">Estado de reportes</h3>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {statusData.slice(0, 8).map((item, index) => (
            <div key={item.name} className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
              <div className="flex items-center justify-between gap-3">
                <span className="truncate text-sm font-medium capitalize text-slate-600">{item.name.replaceAll('_', ' ')}</span>
                <span className="text-sm font-bold text-slate-950">{item.value}</span>
              </div>
              <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white">
                <div
                  className="h-full rounded-full"
                  style={{
                    width: `${Math.min(100, Math.max(8, Number(item.value) * 12))}%`,
                    backgroundColor: CATEGORY_COLORS[index % CATEGORY_COLORS.length],
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
