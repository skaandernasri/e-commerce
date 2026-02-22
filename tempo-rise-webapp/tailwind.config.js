const defaultTheme = require('tailwindcss/defaultTheme')

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      animation: {
        'in': 'in 0.2s ease-out',
        flash: 'flash 0.3s ease-in-out',
        'spin-y': 'spin-y 1.5s linear infinite',
        banner: 'banner var(--speed) linear infinite',
        'fade-in': 'fade-in 0.2s ease-out',
        'slide-in': 'slide-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)',
      },

      screens: {
        'forced-colors': { raw: '(forced-colors: active)' }, // Add forced colors mode support
      },
      colors: {
        // Light theme
        'light-primary': '#3F6212',
        'light-secondary': '#4f46e5',
        'light-background': '#ffffff',
        'light-surface': '#f3f4f6',
        'light-text': '#1f2937',
        'light-text2': '#ffffff',
        'light-footer':'#3F6212',
        'light-hover' : '#4f46e5',
        
        // Dark theme
        'dark-primary': '#60a5fa',
        'dark-secondary': '#818cf8',
        'dark-background': '#111827',
        'dark-surface': '#1f2937',
        'dark-text': '#f9fafb',
        'dark-text2': '#f9fafb',
        'dark-footer':'#1f2937',
        'dark-hover' : '#818cf8',

        
        // Colorful theme
        'colorful-primary': '#f472b6',
        'colorful-secondary': '#fb923c',
        'colorful-background': '#fdf2f8',
        'colorful-surface': '#fce7f3',
        'colorful-text': '#831843',
        'colorful-text2': '#831843',
        'colorful-footer':'#db2777',
        'colorful-hover' : '#fb923c',

        'rating-yellow': {
          400: '#fbbf24', // Your yellow color for stars
        },
        'rating-gray': {
          300: '#d1d5db', // Your gray color for empty stars
        }
      },
      spacing: {
        'star': '1rem', // Default star size
      },
      fontFamily: {
        sans: ['Inter var', ...defaultTheme.fontFamily.sans],
        fantasy : ['fantasy']
      },
      keyframes: {
        'fade-in': {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        'slide-in': {
          '0%': { 
            opacity: '0', 
            transform: 'translateY(-20px) scale(0.95)' 
          },
          '100%': { 
            opacity: '1', 
            transform: 'translateY(0) scale(1)' 
          },
        },
        in: {
          from: {
            opacity: '0',
            transform: 'scale(0.95)',
          },
          to: {
            opacity: '1',
            transform: 'scale(1)',
          },
        },
        flash: {
          '0%, 100%': { opacity: '0' },
          '50%': { opacity: '1' },
        },
        'spin-y': {
          '0%': { transform: 'rotateY(0deg)' },
          '100%': { transform: 'rotateY(360deg)' },
        },
        banner: {
          '0%': { transform: 'translateX(var(--start))'},
        '100%': { transform: 'translateX(var(--end))' }
      },
    }
    },
    screens: {
      'xs': '320px', // custom super small screen
      'sm': '640px',
      'md': '768px',
      'lg': '1024px',
      'xl': '1280px',
    },
  },
  plugins: [
    require('@tailwindcss/line-clamp'),
    require("tailwind-scrollbar")({ nocompatible: true }),
  ],
}