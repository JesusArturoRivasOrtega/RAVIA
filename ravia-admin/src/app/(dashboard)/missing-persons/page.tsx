'use client';

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { missingPersonsApi } from '@/lib/api';
import { cn, relativeTime } from '@/lib/utils';

const STATUS_COLORS: Record<string, string> = {
  pending_review: 'bg-amber-100 text-amber-700',
  active: 'bg-red-100 text-red-700',
  found: 'bg-green-100 text-green-700',
  cancelled: 'bg-gray-100 text-gray-500',
};

const STATUS_LABELS: Record<string, string> = {
  pending_review: 'Pendiente de revision',
  active: 'Busqueda activa',
  found: 'Encontrado/a',
  cancelled: 'Cancelado',
};

export default function MissingPersonsPage() {
  const qc = useQueryClient();

  const { data: persons = [], isLoading } = useQuery({
    queryKey: ['missing-persons'],
    queryFn: () => missingPersonsApi.listAll({ limit: 100 }),
  });

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) =>
      missingPersonsApi.updateStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['missing-persons'] });
      toast.success('Estado actualizado');
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const deletePerson = useMutation({
    mutationFn: (id: string) => missingPersonsApi.delete(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['missing-persons'] });
      toast.success('Ficha eliminada');
    },
    onError: (e: Error) => toast.error(e.message),
  });

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Personas desaparecidas</h1>
        <p className="mt-1 text-gray-500">{persons.length} fichas registradas</p>
      </div>

      {isLoading ? (
        <div className="flex min-h-64 items-center justify-center">
          <div className="h-8 w-8 animate-spin rounded-full border-4 border-navy-700 border-t-transparent" />
        </div>
      ) : persons.length === 0 ? (
        <div className="card p-12 text-center text-gray-400">Sin fichas de personas desaparecidas</div>
      ) : (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
          {persons.map((p: any) => (
            <div key={p.id} className="card p-5">
              <div className="mb-3 flex items-start justify-between gap-4">
                <div>
                  <p className="font-semibold text-gray-900">{p.name}</p>
                  {p.age && <p className="text-sm text-gray-500">{p.age} anos</p>}
                </div>
                <span className={cn('badge', STATUS_COLORS[p.status] ?? STATUS_COLORS.active)}>
                  {STATUS_LABELS[p.status] ?? p.status}
                </span>
              </div>

              <p className="mb-1 text-sm text-gray-600">Ubicacion: {p.lastSeenLocation}</p>
              <p className="line-clamp-2 text-sm text-gray-500">{p.description}</p>

              <div className="mt-3 flex items-center justify-between gap-3 border-t border-gray-100 pt-3">
                <span className="text-xs text-gray-400">
                  {p.sightings?.length ?? 0} avistamientos - {relativeTime(p.createdAt)}
                </span>
                <div className="flex flex-wrap items-center justify-end gap-2">
                  {p.status !== 'active' && (
                    <button
                      onClick={() => updateStatus.mutate({ id: p.id, status: 'active' })}
                      className="text-xs btn-primary px-2 py-1"
                    >
                      Publicar
                    </button>
                  )}
                  {p.status !== 'found' && (
                    <button
                      onClick={() => updateStatus.mutate({ id: p.id, status: 'found' })}
                      className="text-xs btn-primary px-2 py-1"
                    >
                      Marcar encontrado/a
                    </button>
                  )}
                  {p.status !== 'cancelled' && (
                    <button
                      onClick={() => updateStatus.mutate({ id: p.id, status: 'cancelled' })}
                      className="text-xs btn-secondary px-2 py-1"
                    >
                      Cancelar
                    </button>
                  )}
                  <button
                    onClick={() => {
                      if (confirm('Eliminar esta ficha?')) deletePerson.mutate(p.id);
                    }}
                    className="text-xs text-red-600 hover:text-red-800"
                  >
                    Eliminar
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
