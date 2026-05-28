'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { riskZonesApi } from '@/lib/api';
import { relativeTime, cn } from '@/lib/utils';
import { Plus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

const RISK_COLORS: Record<string, string> = {
  low: 'bg-green-100 text-green-700',
  medium: 'bg-amber-100 text-amber-700',
  high: 'bg-orange-100 text-orange-700',
  critical: 'bg-red-100 text-red-700',
};

const RISK_LABELS: Record<string, string> = {
  low: 'Bajo', medium: 'Medio', high: 'Alto', critical: 'Crítico',
};

const defaultForm = { name: '', description: '', riskLevel: 'medium', centerLat: 19.4326, centerLng: -99.1332, radiusMeters: 500 };

export default function RiskZonesPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(defaultForm);

  const { data: zones = [], isLoading } = useQuery({ queryKey: ['risk-zones'], queryFn: riskZonesApi.list });

  const createZone = useMutation({
    mutationFn: () => riskZonesApi.create({ ...form, centerLat: Number(form.centerLat), centerLng: Number(form.centerLng), radiusMeters: Number(form.radiusMeters) }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['risk-zones'] }); setShowForm(false); setForm(defaultForm); toast.success('Zona de riesgo creada'); },
    onError: (e: Error) => toast.error(e.message),
  });

  const deleteZone = useMutation({
    mutationFn: (id: string) => riskZonesApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['risk-zones'] }); toast.success('Zona eliminada'); },
    onError: (e: Error) => toast.error(e.message),
  });

  const field = (key: keyof typeof form, label: string, type = 'text', placeholder = '') => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>
      <input type={type} className="input" value={form[key]} placeholder={placeholder}
        onChange={(e) => setForm({ ...form, [key]: e.target.value })} />
    </div>
  );

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Zonas de Riesgo</h1>
          <p className="text-gray-500 mt-1">{zones.length} zonas activas</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary flex items-center gap-2">
          <Plus className="w-4 h-4" /> Nueva zona
        </button>
      </div>

      {showForm && (
        <div className="card p-5 mb-6 border-l-4 border-navy-700">
          <h3 className="font-semibold text-gray-900 mb-4">Crear zona de riesgo</h3>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="col-span-2">{field('name', 'Nombre *', 'text', 'Ej: Zona norte colonia Roma')}</div>
            <div className="col-span-2">{field('description', 'Descripción')}</div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Nivel de riesgo</label>
              <select className="input" value={form.riskLevel} onChange={(e) => setForm({ ...form, riskLevel: e.target.value })}>
                <option value="low">Bajo</option>
                <option value="medium">Medio</option>
                <option value="high">Alto</option>
                <option value="critical">Crítico</option>
              </select>
            </div>
            <div>{field('radiusMeters', 'Radio (metros)', 'number', '500')}</div>
            <div>{field('centerLat', 'Latitud del centro', 'number', '19.4326')}</div>
            <div>{field('centerLng', 'Longitud del centro', 'number', '-99.1332')}</div>
          </div>
          <div className="flex gap-2">
            <button onClick={() => createZone.mutate()} disabled={!form.name || createZone.isPending} className="btn-primary">
              {createZone.isPending ? 'Creando…' : 'Crear zona'}
            </button>
            <button onClick={() => setShowForm(false)} className="btn-secondary">Cancelar</button>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="flex items-center justify-center min-h-64"><div className="animate-spin w-8 h-8 border-4 border-navy-700 border-t-transparent rounded-full" /></div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {zones.map((z: any) => (
            <div key={z.id} className="card p-5">
              <div className="flex items-start justify-between mb-2">
                <p className="font-semibold text-gray-900">{z.name}</p>
                <span className={cn('badge', RISK_COLORS[z.riskLevel] ?? RISK_COLORS.medium)}>
                  {RISK_LABELS[z.riskLevel] ?? z.riskLevel}
                </span>
              </div>
              <p className="text-sm text-gray-500 mb-3">{z.description}</p>
              <div className="text-xs text-gray-400 space-y-1">
                <p>📍 {z.centerLat?.toFixed(4)}, {z.centerLng?.toFixed(4)}</p>
                <p>⭕ Radio: {z.radiusMeters}m</p>
                <p>📋 {z.reportCount ?? 0} reportes · {relativeTime(z.createdAt)}</p>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-100">
                <button onClick={() => { if (confirm('¿Eliminar esta zona?')) deleteZone.mutate(z.id); }}
                  className="text-xs text-red-500 hover:text-red-700 flex items-center gap-1">
                  <Trash2 className="w-3.5 h-3.5" /> Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
