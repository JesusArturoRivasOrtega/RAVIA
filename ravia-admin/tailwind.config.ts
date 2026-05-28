import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./src/**/*.{js,ts,jsx,tsx,mdx}'],
  theme: {
    extend: {
      colors: {
        navy: {
          50: '#eef2ff',
          100: '#e0e7ff',
          500: '#3b5ea6',
          700: '#1B3A6B',
          900: '#0d1f3c',
        },
        status: {
          pending: '#78909C',
          verifying: '#FFC107',
          confirmed: '#2DC653',
          critical: '#E53935',
          'in-progress': '#1565C0',
          resolved: '#1B5E20',
          false: '#424242',
          duplicated: '#8E24AA',
        },
      },
      fontFamily: { sans: ['Inter', 'system-ui', 'sans-serif'] },
    },
  },
  plugins: [],
};

export default config;
