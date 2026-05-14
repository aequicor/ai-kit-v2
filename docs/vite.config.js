import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'

export default defineConfig({
  base: '/ai-kit-v2/',
  plugins: [react()],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router-dom'],
  },
  css: {
    postcss: {
      plugins: [
        tailwindcss({
          content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
          darkMode: 'class',
          theme: {
            extend: {
              fontFamily: {
                sans: ['Inter', 'system-ui', 'sans-serif'],
                mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
              },
              colors: {
                navy: {
                  900: '#0a0f1e',
                  800: '#0d1530',
                  700: '#111d3f',
                  600: '#162352',
                },
              },
            },
          },
          plugins: [],
        }),
        autoprefixer(),
      ],
    },
  },
})
