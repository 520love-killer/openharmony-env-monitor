/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        deep: { DEFAULT: '#020617', 50: '#08111f', 100: '#0f172a' },
        surface: 'rgba(15,23,42,0.72)',
        accent: { blue: '#38bdf8', cyan: '#22d3ee', purple: '#8b5cf6' },
      },
      fontFamily: {
        sans: ['"Microsoft YaHei"', '"PingFang SC"', '"Segoe UI"', 'Inter', 'sans-serif'],
        mono: ['"Cascadia Code"', '"Fira Code"', 'monospace'],
      },
    },
  },
  plugins: [],
}
