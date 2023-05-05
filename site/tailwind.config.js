/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./_site/**/*.{html,js}",
    "./node_modules/flowbite/**/*.js"
  ],
  theme: {
    // colors: {
    //   link: '#1c7baf',
    // },
    extend: {},
  },
  plugins: [
    require('flowbite/plugin')
  ],
}
