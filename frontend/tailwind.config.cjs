/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./index.html", "./src/**/*.{ts,tsx,js,jsx}"],
  theme: {
    extend: {
      colors: {
        "aqi-good": "#00B050",
        "aqi-moderate": "#92D050",
        "aqi-sensitive": "#FFC000",
        "aqi-unhealthy": "#FF0000",
        "aqi-very-unhealthy": "#7030A0",
        "aqi-hazardous": "#7F0000",
      },
      boxShadow: {
        glow: "0 16px 32px rgba(15, 35, 57, 0.65)",
      },
      fontFamily: {
        rubikDistressed: ['"Rubik Distressed"', 'cursive'],
        bellota: ['"Bellota Text"', 'cursive'],
      },
    },
  },
  plugins: [],
};
