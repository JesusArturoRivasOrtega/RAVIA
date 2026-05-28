'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { missingPersonsApi } from '@/lib/api';
import { relativeTime } from '@/lib/utils';
import { cn } from '@/lib/utils';
import toast from 'react-hot-toast';

const STATUS_COLORS: Record<string, string> = {
  active: 'bg-red-100 text-red-700',
  found: 'bg-green-100 text-green-700',
  cancelled: 'bg-gray-100 text-gray-500',
};

const STATUS_LABELS: Record<string, string> = {
  active: 'Búsqueda activa',
  found: 'Encontrado/a',
  cancelled: 'Cancelado',
};

export default function MissingPersonsPage() {
  const qc = useQueryClient();

  const { data: persons = [], isLoading } = useQuery({
    queryKey: ['missing-persons'],
    queryFn: missingPersonsApi.list,
  });

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      missingPersonsApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['missing-persons'] }); toast.success('Estado actualizado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Personas Desaparecidas</h1>
        <p className="text-gray-500 mt-1">{persons.length} reportes activos</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center min-h-64"><div className="animate-spin w-8 h-8 border-4 border-navy-700 border-t-transparent rounded-full" /></div>
      ) : persons.length === 0 ? (
        <div className="card p-12 text-center text-gray-400">Sin reportes de personas desaparecidas</div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {persons.map((p: any) => (
            <div key={p.id} className="card p-5">
              <div className="flex items-start justify-between gap-4 mb-3">
                <div>
                  <p className="font-semibold text-gray-900">{p.name}</p>
                  {p.age && <p className="text-sm text-gray-500">{p.age} años</p>}
                </div>
                <span className={cn('badge', STATUS_COLORS[p.status] ?? STATUS_COLORS.active)}>
                  {STATUS_LABELS[p.status] ?? p.status}
                </span>
              </div>
              <p className="text-sm text-gray-600 mb-1">📍 {p.lastSeenLocation}</p>
              <p className="text-sm text-gray-500 line-clamp-2">{p.description}</p>
              <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100">
                <span className="text-xs text-gray-400">
                  {p.sightings?.length ?? 0} avistamientos · {relativeTime(p.createdAt)}
                </span>
                <div className="flex items-center gap-2">
                  {p.status === 'active' && (
                    <>
                      <button
                        onClick={() => updateStatus.mutate({ id: p.id, status: 'found' })}
                        className="text-xs btn-primary py-1 px-2"
                      >
                        Marcar encontrado/a
                      </button>
                      <button
                        onClick={() => updateStatus.mutate({ id: p.id, status: 'cancelled' })}
                        className="text-xs btn-secondary py-1 px-2"
                      >
                        Cancelar
                      </button>
                    </>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
