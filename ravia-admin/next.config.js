/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: { typedRoutes: false },
  images: { domains: ['firebasestorage.googleapis.com', 'lh3.googleusercontent.com'] },
};

module.exports = nextConfig;
