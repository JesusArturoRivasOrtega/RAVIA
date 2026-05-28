'use client';

import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { BellOff, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { RaviaGlyph } from '@/components/brand/RaviaGlyph';
import { SeverityBadge } from '@/components/ui/StatusBadge';
import { alertsApi } from '@/lib/api';
import { relativeTime } from '@/lib/utils';
import { Alert, AlertSeverity } from '@/types';

export default function AlertsPage() {
  const qc = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', severity: AlertSeverity.INFO });

  const { data: alerts = [], isLoading } = useQuery<Alert[]>({
    queryKey: ['alerts'],
    queryFn: alertsApi.listAll,
  });

  const createAlert = useMutation({
    mutationFn: () => alertsApi.create(form),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['alerts'] });
      setShowForm(false);
      setForm({ title: '', description: '', severity: AlertSeverity.INFO });
      toast.success('Alerta creada');
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const deactivate = useMutation({
    mutationFn: (id: string) => alertsApi.deactivate(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['alerts'] });
      toast.success('Alerta desactivada');
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const activeCount = alerts.filter((alert) => alert.isActive).length;

  return (
    <div className="space-y-6 p-6 lg:p-8">
      <div className="panel-enter flex flex-col gap-4 rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-normal text-slate-950">Alertas</h1>
          <p className="mt-1 text-sm text-slate-500">{activeCount} activas</p>
        </div>
        <button onClick={() => setShowForm(true)} className="btn-primary flex items-center gap-2">
          <Plus className="h-4 w-4" /> Nueva alerta
        </button>
      </div>

      {showForm && (
        <div className="card panel-enter border-l-4 border-navy-700 p-5">
          <h3 className="mb-4 font-semibold text-slate-950">Crear nueva alerta</h3>
          <div className="mb-4 grid grid-cols-2 gap-4">
            <label className="col-span-2 block">
              <span className="mb-1 block text-sm font-medium text-slate-700">Título *</span>
              <input
                className="input"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="Ej: Inundación en zona norte"
              />
            </label>
            <label className="col-span-2 block">
              <span className="mb-1 block text-sm font-medium text-slate-700">Descripción *</span>
              <textarea
                className="input"
                rows={3}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="Describe la alerta con detalle"
              />
            </label>
            <label className="block">
              <span className="mb-1 block text-sm font-medium text-slate-700">Severidad</span>
              <select
                className="input"
                value={form.severity}
                onChange={(e) => setForm({ ...form, severity: e.target.value as AlertSeverity })}
              >
                <option value={AlertSeverity.INFO}>Informativo</option>
                <option value={AlertSeverity.CAUTION}>Precaución</option>
                <option value={AlertSeverity.URGENT}>Urgente</option>
                <option value={AlertSeverity.CRITICAL}>Crítico</option>
              </select>
            </label>
          </div>
          <div className="flex gap-2">
            <button onClick={() => createAlert.mutate()} disabled={!form.title || !form.description || createAlert.isPending} className="btn-primary">
              {createAlert.isPending ? 'Creando...' : 'Publicar alerta'}
            </button>
            <button onClick={() => setShowForm(false)} className="btn-secondary">Cancelar</button>
          </div>
        </div>
      )}

      {isLoading ? (
        <div className="grid min-h-64 place-items-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-navy-700 border-t-transparent" />
        </div>
      ) : alerts.length === 0 ? (
        <div className="card panel-enter grid min-h-72 place-items-center p-12 text-center">
          <div>
            <div className="mx-auto grid h-12 w-12 place-items-center rounded-lg bg-slate-100 text-slate-500">
              <RaviaGlyph name="alerts" />
            </div>
            <p className="mt-4 font-medium text-slate-950">No hay alertas publicadas</p>
            <p className="mt-1 text-sm text-slate-500">Crea una alerta cuando exista una condición relevante.</p>
          </div>
        </div>
      ) : (
        <div className="grid gap-3">
          {alerts.map((alert) => (
            <div
              key={alert.id}
              className={`card interactive-card panel-enter flex items-start justify-between gap-4 p-4 ${!alert.isActive ? 'opacity-60' : ''}`}
            >
              <div className="min-w-0 flex-1">
                <div className="mb-2 flex flex-wrap items-center gap-2">
                  <SeverityBadge severity={alert.severity} />
                  {!alert.isActive && <span className="badge bg-slate-100 text-slate-500">Inactiva</span>}
                </div>
                <p className="font-medium text-slate-950">{alert.title}</p>
                <p className="mt-0.5 text-sm text-slate-500">{alert.description}</p>
                <p className="mt-2 text-xs text-slate-400">{relativeTime(alert.createdAt)}</p>
              </div>
              {alert.isActive && (
                <button onClick={() => deactivate.mutate(alert.id)} className="btn-secondary flex items-center gap-1.5 text-xs">
                  <BellOff className="h-3.5 w-3.5" /> Desactivar
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
