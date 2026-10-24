# 🛡️ VanSwift - The Ultimate Free & Open-Source Android Backup Solution

**VanSwift** adalah aplikasi pencadangan (backup & restore) sistem dan aplikasi Android modern berbasis Jetpack Compose yang mengimplementasikan **100% fungsionalitas setara aplikasi backup tingkat lanjut**, sepenuhnya **gratis, tanpa registrasi akun/sign-up, dan tanpa biaya berlangganan apapun**.

---

## 🌟 Fitur Utama (Semua Fitur Premium Terbuka Gratis)

1. **Pencadangan Aplikasi Lengkap:**
   - Base APK + Split APKs / APKs bundle.
   - Data internal aplikasi (`/data/data/<pkg>`) terkompresi `tar.gz` (via Root atau Shizuku).
   - Ekstraksi izin (*permissions*) dan konfigurasi SSAID.
2. **Multi-Version History & Protection:**
   - Menyimpan beberapa riwayat versi cadangan per aplikasi (*dated backups*).
   - Opsi proteksi cadangan (*Protected Backups*) agar tidak terhapus saat pembersihan massal.
3. **Penyimpanan Cloud Langsung (Self-Hosted & Private):**
   - **WebDAV / Nextcloud / ownCloud**
   - **SMB / Windows Share**
   - **SFTP / SSH Server**
   - **Amazon S3 / MinIO / Cloudflare R2**
   - Tanpa perantara pihak ketiga / backend Firebase.
4. **Penjadwalan Otomatis (WorkManager):**
   - Eksekusi backup berkala (harian, mingguan).
   - Filter pemicu: hanya saat pengisian daya (*charging*), hanya di jaringan Wi-Fi, atau saat baterai di atas persentase tertentu.
5. **Pencadangan Sistem Lengkap:**
   - Pesan SMS & MMS (Telephony Provider API).
   - Riwayat Panggilan Telepon (*Call Logs*).
   - Konfigurasi sandi Wi-Fi tersimpan.
6. **Keamanan & Privasi:**
   - Enkripsi lokal berbasis sandi menggunakan **AES-256 GCM + PBKDF2**.
   - Bebas pelacak (*no analytics, no ads, no trackers*).

---

## 🚀 Cara Kompilasi dan Build Menjadi APK

### Opsi A: Menggunakan Android Studio (Direkomendasikan)
1. Salin atau unduh folder `/root/VanSwift` ke PC Anda.
2. Buka **Android Studio**.
3. Pilih **Open an Existing Project** dan arahkan ke folder `VanSwift`.
4. Tunggu proses *Gradle Sync* selesai.
5. Klik menu **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
6. File APK siap di-install di perangkat Android Anda!

### Opsi B: Menggunakan Gradle CLI
```bash
cd VanSwift
./gradlew assembleRelease
```
File APK akan berada di: `app/build/outputs/apk/release/app-release.apk`
