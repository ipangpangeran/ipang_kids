# 🚀 Portal Admin & REST API Kuis Ipang Kids

Project ini adalah Backend REST API dan Web Portal Admin berbasis **Node.js + Express + SQLite** yang sangat ringan (konsumsi RAM < 40MB). 
Portal ini memungkinkan Anda menambah, mengedit, dan menghapus konten kuis (Matematika & Membaca) secara **real-time** tanpa perlu melakukan build ulang file APK Android!

Domain Target: `https://api.ipangpangeran.com`

---

## 💻 Fitur Portal Admin:
- **Tampilan Web Responsive & Modern**: Dashboard pengelolaan kuis matematika dan membaca.
- **REST API Siap Pakai**:
  - `GET /api/math` - Mengambil daftar kuis matematika (dukungan filter kategori/kesulitan).
  - `POST /api/math` - Menambah soal matematika baru.
  - `PUT /api/math/:id` - Mengubah soal matematika.
  - `DELETE /api/math/:id` - Menghapus soal matematika.
  - `GET /api/reading` - Mengambil daftar kuis membaca.
  - `POST /api/reading` - Menambah soal membaca baru.
  - `PUT /api/reading/:id` - Mengubah soal membaca.
  - `DELETE /api/reading/:id` - Menghapus soal membaca.
- **Proteksi Passkey Admin**: Keamanan endpoint mutasi data menggunakan Passkey Admin (`ipang123`).
- **Auto-Seeder Initial Data**: Otomatis mengisi 15 soal matematika & 15 soal membaca pada booting pertama.

---

## 🛠️ Cara Deploy ke Server Ubuntu Public:

### Opsi A: Menggunakan PM2 (Rekomendasi Paling Ringan)

1. **Clone repository di server Ubuntu Anda**:
   ```bash
   git clone <URL_GIT_REPOSITORY_ANDA>
   cd ipang_kids/backend-admin
   ```

2. **Install Node.js & PM2 (jika belum ada)**:
   ```bash
   curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
   sudo apt-get install -y nodejs
   sudo npm install -g pm2
   ```

3. **Install Dependencies & Jalankan Server**:
   ```bash
   npm install --production
   pm2 start ecosystem.config.js
   pm2 save
   pm2 startup
   ```

---

### Opsi B: Menggunakan Docker Compose

```bash
cd ipang_kids/backend-admin
docker-compose up -d --build
```

---

## 🔒 Konfigurasi Nginx & HTTPS SSL (Certbot) untuk `api.ipangpangeran.com`

1. **Install Nginx & Certbot**:
   ```bash
   sudo apt update
   sudo apt install -y nginx certbot python3-certbot-nginx
   ```

2. **Buat file konfigurasi Nginx**:
   ```bash
   sudo nano /etc/nginx/sites-available/api.ipangpangeran.com
   ```
   Isi dengan konfigurasi berikut:
   ```nginx
   server {
       server_name api.ipangpangeran.com;

       location / {
           proxy_pass http://localhost:3000;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
       }
   }
   ```

3. **Aktifkan situs & Dapatkan SSL Gratis Let's Encrypt**:
   ```bash
   sudo ln -s /etc/nginx/sites-available/api.ipangpangeran.com /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl reload nginx

   # Aktifkan SSL HTTPS otomatis
   sudo certbot --nginx -d api.ipangpangeran.com
   ```

4. Buka browser dan akses **`https://api.ipangpangeran.com`**!Masukkan Passkey: `ipang123` untuk mengelola kuis.
