'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { onAuthStateChanged, type Auth } from 'firebase/auth';
import { RaviaMark } from '@/components/brand/RaviaMark';
import { Sidebar } from '@/components/layout/Sidebar';
import { useAuthStore } from '@/lib/auth-store';
import { usersApi } from '@/lib/api';
import { requireFirebaseAuth } from '@/lib/firebase';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { user, setUser, setLoading } = useAuthStore();

  useEffect(() => {
    let auth: Auth;
    try {
      auth = requireFirebaseAuth();
    } catch {
      setUser(null);
      setLoading(false);
      router.replace('/login');
      return;
    }

    const unsub = onAuthStateChanged(auth, async (firebaseUser) => {
      if (!firebaseUser) {
        setUser(null);
        setLoading(false);
        router.replace('/login');
        return;
      }

      try {
        const profile = await usersApi.get('me');
        if (profile.role === 'citizen') {
          await auth.signOut();
          router.replace('/login');
          return;
        }
        setUser(profile);
      } catch {
        router.replace('/login');
      } finally {
        setLoading(false);
      }
    });

    return () => unsub();
  }, [router, setLoading, setUser]);

  if (!user) {
    return (
      <div className="ravia-scan grid min-h-screen place-items-center bg-[#f5f7fa]">
        <div className="panel-enter flex flex-col items-center gap-4">
          <RaviaMark className="h-16 w-16 shadow-lg" />
          <div className="h-1.5 w-36 overflow-hidden rounded-full bg-slate-200">
            <div className="h-full w-1/2 animate-pulse rounded-full bg-navy-700" />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-[#f5f7fa]">
      <Sidebar />
      <main className="ravia-scan min-w-0 flex-1 overflow-auto">
        {children}
      </main>
    </div>
  );
}
