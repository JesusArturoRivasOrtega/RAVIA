'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ChevronRight, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import { RaviaGlyph } from '@/components/brand/RaviaGlyph';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { reportsApi } from '@/lib/api';
import { allowedReportStatusTransitions, CATEGORY_LABELS, relativeTime, STATUS_LABELS } from '@/lib/utils';
import { Report, ReportStatus } from '@/types';

const STATUS_OPTIONS = Object.values(ReportStatus);

export default function ReportsPage() {
  const qc = useQueryClient();
  const [statusFilter, setStatusFilter] = useState<string>('');

  const { data: reports = [], isLoading } = useQuery<Report[]>({
    queryKey: ['reports', statusFilter],
    queryFn: () => reportsApi.list(statusFilter ? { status: statusFilter } : {}),
  });

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => reportsApi.updateStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['reports'] });
      toast.success('Estado actualizado');
    },
    onError: (err: Error) => toast.error(err.message),
  });

  return (
    <div className="space-y-6 p-6 lg:p-8">
      <div className="panel-enter flex flex-col gap-4 rounded-lg border border-slate-200 bg-white px-5 py-4 shadow-sm lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-normal text-slate-950">Reportes</h1>
          <p className="mt-1 text-sm text-slate-500">{reports.length} reportes encontrados</p>
        </div>
        <label className="flex items-center gap-3">
          <Filter className="h-4 w-4 text-slate-400" />
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className="input w-48">
            <option value="">Todos los estados</option>
            {STATUS_OPTIONS.map((s) => (
              <option key={s} value={s}>{STATUS_LABELS[s] ?? s}</option>
            ))}
          </select>
        </label>
      </div>

      {isLoading ? (
        <div className="grid min-h-64 place-items-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-navy-700 border-t-transparent" />
        </div>
      ) : reports.length === 0 ? (
        <div className="card panel-enter grid min-h-72 place-items-center p-12 text-center">
          <div>
            <div className="mx-auto grid h-12 w-12 place-items-center rounded-lg bg-slate-100 text-slate-500">
              <RaviaGlyph name="reports" />
            </div>
            <p className="mt-4 font-medium text-slate-950">No hay reportes con esos filtros</p>
            <p className="mt-1 text-sm text-slate-500">Cambia el estado seleccionado para ampliar la búsqueda.</p>
          </div>
        </div>
      ) : (
        <div className="card panel-enter overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full min-w-[900px] text-sm">
              <thead>
                <tr className="border-b border-slate-100 bg-slate-50">
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Reporte</th>
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Categoría</th>
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Estado</th>
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Validación</th>
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Fecha</th>
                  <th className="px-4 py-3 text-left font-medium text-slate-500">Acción rápida</th>
                  <th className="px-4 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reports.map((report) => {
                  const transitions = allowedReportStatusTransitions(report.status);
                  return (
                  <tr key={report.id} className="transition-colors hover:bg-slate-50/80">
                    <td className="px-4 py-3">
                      <p className="max-w-60 truncate font-medium text-slate-950">{report.title}</p>
                      <p className="max-w-60 truncate text-xs text-slate-400">
                        {report.address ?? 'Sin calle registrada'}
                        {report.location ? ` · ${report.location.lat.toFixed(4)}, ${report.location.lng.toFixed(4)}` : ''}
                      </p>
                    </td>
                    <td className="px-4 py-3 text-slate-600">{CATEGORY_LABELS[report.category] ?? report.category}</td>
                    <td className="px-4 py-3"><StatusBadge status={report.status} /></td>
                    <td className="px-4 py-3">
                      <div className="flex items-center gap-2">
                        <span className="rounded-full bg-green-50 px-2 py-0.5 text-xs font-semibold text-green-700">{report.confirmCount} ok</span>
                        <span className="rounded-full bg-red-50 px-2 py-0.5 text-xs font-semibold text-red-600">{report.falseCount} falso</span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-400">{relativeTime(report.createdAt)}</td>
                    <td className="px-4 py-3">
                      <select
                        defaultValue=""
                        disabled={transitions.length === 0 || updateStatus.isPending}
                        onChange={(e) => {
                          if (e.target.value) updateStatus.mutate({ id: report.id, status: e.target.value });
                        }}
                        className="rounded-md border border-slate-200 bg-white px-2 py-1 text-xs text-slate-700 disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-400"
                      >
                        <option value="">{transitions.length ? 'Cambiar estado' : 'Estado final'}</option>
                        {transitions.map((s) => (
                          <option key={s} value={s}>{STATUS_LABELS[s]}</option>
                        ))}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <Link href={`/reports/${report.id}`} className="grid h-8 w-8 place-items-center rounded-md text-navy-700 transition-colors hover:bg-navy-50">
                        <ChevronRight className="h-4 w-4" />
                      </Link>
                    </td>
                  </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
