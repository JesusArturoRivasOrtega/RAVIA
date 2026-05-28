'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, useRouter } from 'next/navigation';
import { reportsApi } from '@/lib/api';
import { StatusBadge } from '@/components/ui/StatusBadge';
import { ReportStatus } from '@/types';
import { allowedReportStatusTransitions, formatDate, CATEGORY_LABELS, STATUS_LABELS } from '@/lib/utils';
import { ArrowLeft, Brain, MapPin, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import Link from 'next/link';

export default function ReportDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const qc = useQueryClient();

  const { data: report, isLoading } = useQuery({
    queryKey: ['report', id],
    queryFn: () => reportsApi.get(id),
  });

  const updateStatus = useMutation({
    mutationFn: (status: string) => reportsApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', id] }); toast.success('Estado actualizado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  const analyze = useMutation({
    mutationFn: () => reportsApi.analyze(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', id] }); toast.success('Análisis completado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  const deleteReport = useMutation({
    mutationFn: () => reportsApi.delete(id),
    onSuccess: () => { router.push('/reports'); toast.success('Reporte eliminado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  if (isLoading) {
    return <div className="p-8 flex items-center justify-center min-h-96"><div className="animate-spin w-8 h-8 border-4 border-navy-700 border-t-transparent rounded-full" /></div>;
  }

  if (!report) return <div className="p-8 text-gray-400">Reporte no encontrado</div>;

  const transitions = allowedReportStatusTransitions(report.status);

  return (
    <div className="p-8 max-w-4xl">
      <div className="flex items-center gap-3 mb-6">
        <Link href="/reports" className="text-gray-400 hover:text-gray-600">
          <ArrowLeft className="w-5 h-5" />
        </Link>
        <div>
          <h1 className="text-xl font-bold text-gray-900">{report.title}</h1>
          <p className="text-gray-400 text-sm">ID: {report.id}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main info */}
        <div className="lg:col-span-2 space-y-4">
          <div className="card p-5">
            <div className="flex items-center gap-3 mb-4">
              <StatusBadge status={report.status} />
              <span className="text-sm text-gray-500">{CATEGORY_LABELS[report.category] ?? report.category}</span>
              <span className="text-sm text-gray-400 ml-auto">{formatDate(report.createdAt)}</span>
            </div>
            <p className="text-gray-700">{report.description}</p>
          </div>

          {/* AI Analysis */}
          {report.aiAnalysis && (
            <div className="card p-5 border-l-4 border-navy-700">
              <h3 className="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                <Brain className="w-4 h-4 text-navy-700" /> Análisis IA
              </h3>
              <p className="text-sm text-gray-600 mb-2">{report.aiAnalysis.summary}</p>
              <div className="flex items-center gap-2">
                <div className="flex-1 bg-gray-100 rounded-full h-2">
                  <div
                    className="bg-navy-700 rounded-full h-2"
                    style={{ width: `${Math.round((report.aiAnalysis.confidence ?? 0) * 100)}%` }}
                  />
                </div>
                <span className="text-xs text-gray-500 font-medium">
                  {Math.round((report.aiAnalysis.confidence ?? 0) * 100)}% confianza
                </span>
              </div>
            </div>
          )}

          {/* Status history */}
          {report.statusHistory?.length > 0 && (
            <div className="card p-5">
              <h3 className="font-semibold text-gray-900 mb-3">Historial de estados</h3>
              <div className="space-y-2">
                {report.statusHistory.map((h: any, i: number) => (
                  <div key={i} className="flex items-center gap-3 text-sm">
                    <span className="w-2 h-2 rounded-full bg-navy-700 flex-shrink-0" />
                    <span className="font-medium">{STATUS_LABELS[h.status as ReportStatus] ?? h.status}</span>
                    {h.reason && <span className="text-gray-400">— {h.reason}</span>}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Actions sidebar */}
        <div className="space-y-4">
          <div className="card p-5 text-sm text-gray-500">
            <h3 className="mb-3 flex items-center gap-2 font-semibold text-gray-900">
              <MapPin className="h-4 w-4 text-navy-700" /> Ubicacion
            </h3>
            <p className="font-medium text-gray-800">{report.address ?? 'Sin calle registrada'}</p>
            {report.location && (
              <p className="mt-1 text-xs text-gray-400">
                {report.location.lat.toFixed(5)}, {report.location.lng.toFixed(5)}
              </p>
            )}
          </div>

          <div className="card p-5">
            <h3 className="font-semibold text-gray-900 mb-3">Cambiar estado</h3>
            {transitions.length === 0 ? (
              <p className="text-sm text-gray-400">Este reporte ya esta cerrado.</p>
            ) : (
            <div className="space-y-2">
              {transitions.map((s) => (
                <button
                  key={s}
                  onClick={() => updateStatus.mutate(s)}
                  disabled={updateStatus.isPending}
                  className="w-full text-left px-3 py-2 text-sm rounded-lg hover:bg-gray-50 border border-gray-200 transition-colors"
                >
                  {STATUS_LABELS[s]}
                </button>
              ))}
            </div>
            )}
          </div>

          <div className="card p-5 space-y-2">
            <h3 className="font-semibold text-gray-900 mb-3">Acciones</h3>
            <button
              onClick={() => analyze.mutate()}
              disabled={analyze.isPending}
              className="btn-primary w-full flex items-center justify-center gap-2 text-sm"
            >
              <Brain className="w-4 h-4" />
              {analyze.isPending ? 'Analizando…' : 'Analizar con IA'}
            </button>
            <button
              onClick={() => { if (confirm('¿Eliminar este reporte?')) deleteReport.mutate(); }}
              className="btn-danger w-full flex items-center justify-center gap-2 text-sm"
            >
              <Trash2 className="w-4 h-4" /> Eliminar
            </button>
          </div>

          <div className="card p-5 text-sm text-gray-500 space-y-1.5">
            <div className="flex justify-between"><span>Confirmaciones</span><span className="text-green-600 font-medium">{report.confirmCount}</span></div>
            <div className="flex justify-between"><span>Reportes falsos</span><span className="text-red-500 font-medium">{report.falseCount}</span></div>
            <div className="flex justify-between"><span>Anónimo</span><span>{report.isAnonymous ? 'Sí' : 'No'}</span></div>
          </div>
        </div>
      </div>
    </div>
  );
}
