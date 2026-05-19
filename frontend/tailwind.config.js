/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        tech: {
          blue: '#2563EB',
          cyan: '#06B6D4',
          purple: '#7C3AED',
          orange: '#F97316',
          sky: '#0EA5E9',
          violet: '#8B5CF6',
          green: '#10B981',
          red: '#EF4444',
        },
      },
      fontFamily: {
        sans: ['"Microsoft YaHei"', '"Segoe UI"', 'Inter', 'sans-serif'],
        mono: ['"Cascadia Code"', '"Fira Code"', 'monospace'],
      },
    },
  },
  plugins: [],
}
