module.exports = {
  apps: [{
    name: 'ipang-kids-admin',
    script: 'server.js',
    instances: 1,
    autorestart: true,
    watch: false,
    max_memory_restart: '150M',
    env: {
      NODE_ENV: 'production',
      PORT: 8765,
      ADMIN_KEY: 'ipang123'
    }
  }]
};
