'use client';

import { useQuery } from '@tanstack/react-query';
import { statisticsApi } from '@/lib/api';
import { CATEGORY_LABELS, STATUS_LABELS } from '@/lib/utils';
import { ReportStatus } from '@/types';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, PieChart, Pie, Cell, Legend
} from 'recharts';

const PIE_COLORS = ['#1B3A6B', '#3b5ea6', '#2DC653', '#FFC107', '#E53935', '#8E24AA', '#1565C0', '#FF6D00', '#00ACC1', '#78909C'];

export default function StatisticsPage() {
  const { data: stats } = useQuery({ queryKey: ['stats-full'], queryFn: statisticsApi.dashboard, refetchInterval: 60000 });
  const { data: trends7 } = useQuery({ queryKey: ['trends-7'], queryFn: () => statisticsApi.trends(7) });
  const { data: trends30 } = useQuery({ queryKey: ['trends-30'], queryFn: () => statisticsApi.trends(30) });

  const categoryData = Object.entries(stats?.reportsByCategory ?? {}).map(([key, value]) => ({
    name: CATEGORY_LABELS[key]?.replace(/^.{2}/, '') ?? key,
    value: value as number,
  }));

  const statusData = Object.entries(stats?.reportsByStatus ?? {}).map(([key, value]) => ({
    name: STATUS_LABELS[key as ReportStatus] ?? key,
    value: value as number,
  }));

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Estadísticas</h1>
        <p className="text-gray-500 mt-1">Análisis detallado de la actividad en RAVIA</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 7 day trend */}
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 mb-4">Tendencia — últimos 7 días</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={trends7 ?? []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Bar dataKey="count" fill="#1B3A6B" radius={[4, 4, 0, 0]} name="Reportes" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* 30 day trend */}
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 mb-4">Tendencia — últimos 30 días</h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={trends30 ?? []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 10 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Line type="monotone" dataKey="count" stroke="#1B3A6B" strokeWidth={2} dot={false} name="Reportes" />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* By category */}
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 mb-4">Por categoría</h3>
          {categoryData.length > 0 ? (
            <div className="flex items-center gap-4">
              <ResponsiveContainer width="55%" height={220}>
                <PieChart>
                  <Pie data={categoryData} cx="50%" cy="50%" innerRadius={55} outerRadius={85} paddingAngle={3} dataKey="value">
                    {categoryData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="space-y-1.5 text-xs flex-1">
                {categoryData.map((item, i) => (
                  <div key={i} className="flex items-center gap-2">
                    <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }} />
                    <span className="text-gray-600 flex-1 truncate">{item.name}</span>
                    <span className="font-semibold text-gray-900">{item.value}</span>
                  </div>
                ))}
              </div>
            </div>
          ) : <p className="text-gray-400 text-sm py-8 text-center">Sin datos</p>}
        </div>

        {/* By status */}
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 mb-4">Por estado</h3>
          {statusData.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={statusData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" horizontal={false} />
                <XAxis type="number" tick={{ fontSize: 11 }} />
                <YAxis dataKey="name" type="category" tick={{ fontSize: 10 }} width={80} />
                <Tooltip />
                <Bar dataKey="value" fill="#3b5ea6" radius={[0, 4, 4, 0]} name="Reportes" />
              </BarChart>
            </ResponsiveContainer>
          ) : <p className="text-gray-400 text-sm py-8 text-center">Sin datos</p>}
        </div>
      </div>
    </div>
  );
}
