import Image from 'next/image';
import { cn } from '@/lib/utils';

interface RaviaMarkProps {
  className?: string;
  imageClassName?: string;
}

export function RaviaMark({ className, imageClassName }: RaviaMarkProps) {
  return (
    <div
      className={cn(
        'relative grid place-items-center overflow-hidden rounded-lg bg-[#0A1628] ring-1 ring-white/15',
        className,
      )}
    >
      <Image
        src="/brand/ravia-guard-dog-logo.png"
        alt="RAVIA"
        width={128}
        height={128}
        priority
        className={cn('h-full w-full object-cover', imageClassName)}
      />
      <span className="pointer-events-none absolute inset-0 bg-gradient-to-br from-white/10 via-transparent to-cyan-400/10" />
    </div>
  );
}
