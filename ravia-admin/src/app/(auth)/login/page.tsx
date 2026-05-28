'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { signInWithEmailAndPassword } from 'firebase/auth';
import { Lock, Mail } from 'lucide-react';
import toast from 'react-hot-toast';
import { RaviaGlyph, type RaviaGlyphName } from '@/components/brand/RaviaGlyph';
import { RaviaMark } from '@/components/brand/RaviaMark';
import { usersApi } from '@/lib/api';
import { useAuthStore } from '@/lib/auth-store';
import { requireFirebaseAuth } from '@/lib/firebase';

export default function LoginPage() {
  const router = useRouter();
  const { setUser } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const auth = requireFirebaseAuth();
      await signInWithEmailAndPassword(auth, email, password);
      const profile = await usersApi.get('me');

      if (profile.role === 'citizen') {
        toast.error('No tienes permisos de administrador');
        await auth.signOut();
        return;
      }

      setUser(profile);
      router.push('/dashboard');
    } catch (err: any) {
      toast.error(err.message ?? 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="ravia-scan grid min-h-screen place-items-center bg-[#0A1628] p-4">
      <div className="panel-enter grid w-full max-w-5xl overflow-hidden rounded-lg border border-white/10 bg-white shadow-2xl md:grid-cols-[0.95fr_1.05fr]">
        <section className="hidden bg-[#0A1628] p-8 text-white md:flex md:flex-col md:justify-between">
          <div>
            <RaviaMark className="h-16 w-16 shadow-xl" />
            <h1 className="mt-6 text-3xl font-bold tracking-normal">RAVIA Admin</h1>
            <p className="mt-3 max-w-sm text-sm leading-6 text-white/65">
              Monitorea reportes, valida alertas y coordina respuesta comunitaria desde un panel claro.
            </p>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {([
              ['reports', 'Reportes'],
              ['alerts', 'Alertas'],
              ['zones', 'Zonas'],
            ] as Array<[RaviaGlyphName, string]>).map(([glyph, label]) => (
              <div key={label} className="rounded-lg border border-white/10 bg-white/[0.06] p-3">
                <RaviaGlyph name={glyph} className="h-5 w-5 text-cyan-300" />
                <p className="mt-2 text-xs text-white/65">{label}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="p-6 sm:p-8">
          <div className="mb-8 flex items-center gap-3 md:hidden">
            <RaviaMark className="h-11 w-11" />
            <div>
              <p className="font-bold leading-none text-slate-950">RAVIA Admin</p>
              <p className="mt-1 text-xs text-slate-500">Centro de comando</p>
            </div>
          </div>

          <div className="mb-7">
            <p className="text-sm font-medium text-navy-700">Acceso seguro</p>
            <h2 className="mt-2 text-2xl font-bold tracking-normal text-slate-950">Iniciar sesión</h2>
            <p className="mt-2 text-sm text-slate-500">Solo moderadores y administradores tienen acceso.</p>
          </div>

          <form onSubmit={handleLogin} className="space-y-4">
            <label className="block">
              <span className="mb-1.5 block text-sm font-medium text-slate-700">Correo electrónico</span>
              <div className="relative">
                <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="input pl-10"
                  placeholder="admin@ravia.app"
                  autoComplete="email"
                  required
                />
              </div>
            </label>

            <label className="block">
              <span className="mb-1.5 block text-sm font-medium text-slate-700">Contraseña</span>
              <div className="relative">
                <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="input pl-10"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  required
                />
              </div>
            </label>

            <button type="submit" disabled={loading} className="btn-primary w-full py-2.5">
              {loading ? 'Iniciando sesión...' : 'Ingresar al panel'}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}
