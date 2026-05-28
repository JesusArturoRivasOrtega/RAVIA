'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usersApi } from '@/lib/api';
import { UserRole } from '@/types';
import { relativeTime } from '@/lib/utils';
import { cn } from '@/lib/utils';
import toast from 'react-hot-toast';

const ROLE_COLORS: Record<UserRole, string> = {
  [UserRole.CITIZEN]: 'bg-gray-100 text-gray-600',
  [UserRole.MODERATOR]: 'bg-blue-100 text-blue-700',
  [UserRole.ADMIN]: 'bg-purple-100 text-purple-700',
};

const ROLE_LABELS: Record<UserRole, string> = {
  [UserRole.CITIZEN]: 'Ciudadano',
  [UserRole.MODERATOR]: 'Moderador',
  [UserRole.ADMIN]: 'Administrador',
};

export default function UsersPage() {
  const qc = useQueryClient();

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: usersApi.list,
  });

  const updateRole = useMutation({
    mutationFn: ({ id, role }: { id: string; role: string }) => usersApi.updateRole(id, role),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); toast.success('Rol actualizado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  const updateStatus = useMutation({
    mutationFn: ({ id, status }: { id: string; status: string }) => usersApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); toast.success('Estado actualizado'); },
    onError: (e: Error) => toast.error(e.message),
  });

  return (
    <div className="p-8">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Usuarios</h1>
        <p className="text-gray-500 mt-1">{users.length} usuarios registrados</p>
      </div>

      {isLoading ? (
        <div className="flex items-center justify-center min-h-64"><div className="animate-spin w-8 h-8 border-4 border-navy-700 border-t-transparent rounded-full" /></div>
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="text-left px-4 py-3 font-medium text-gray-500">Usuario</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Rol</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Estado</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Reputación</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Reportes</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Registro</th>
                <th className="text-left px-4 py-3 font-medium text-gray-500">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {users.map((u: any) => (
                <tr key={u.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 bg-navy-700/10 rounded-full flex items-center justify-center text-navy-700 font-medium text-sm">
                        {u.displayName?.[0]?.toUpperCase() ?? '?'}
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">{u.displayName}</p>
                        <p className="text-gray-400 text-xs">{u.email}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className={cn('badge', ROLE_COLORS[u.role as UserRole] ?? ROLE_COLORS[UserRole.CITIZEN])}>
                      {ROLE_LABELS[u.role as UserRole] ?? u.role}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={cn('badge', u.status === 'active' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700')}>
                      {u.status === 'active' ? 'Activo' : u.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-gray-600 font-medium">{u.reputationPoints ?? 0} pts</td>
                  <td className="px-4 py-3 text-gray-600">{u.reportCount ?? 0}</td>
                  <td className="px-4 py-3 text-gray-400 text-xs">{relativeTime(u.createdAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <select
                        defaultValue=""
                        onChange={(e) => { if (e.target.value) updateRole.mutate({ id: u.id, role: e.target.value }); }}
                        className="text-xs border border-gray-200 rounded px-2 py-1 bg-white"
                      >
                        <option value="">Cambiar rol…</option>
                        <option value="citizen">Ciudadano</option>
                        <option value="moderator">Moderador</option>
                        <option value="admin">Admin</option>
                      </select>
                      {u.status !== 'active' && (
                        <button onClick={() => updateStatus.mutate({ id: u.id, status: 'active' })} className="text-xs text-green-600 hover:text-green-800">Activar</button>
                      )}
                      {u.status !== 'suspended' && (
                        <button onClick={() => updateStatus.mutate({ id: u.id, status: 'suspended' })} className="text-xs text-red-500 hover:text-red-700">Suspender</button>
                      )}
                      {u.status !== 'banned' && (
                        <button onClick={() => updateStatus.mutate({ id: u.id, status: 'banned' })} className="text-xs text-red-700 hover:text-red-900">Banear</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
