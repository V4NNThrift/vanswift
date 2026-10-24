package com.vanswift.backup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanswift.backup.core.privilege.ShellExecutor
import com.vanswift.backup.model.AppItem
import com.vanswift.backup.ui.theme.VanSwiftTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VanSwiftTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Dashboard", "Aplikasi", "Cloud Sync", "Jadwal", "Pengaturan")
    val tabIcons = listOf(Icons.Default.Dashboard, Icons.Default.Apps, Icons.Default.Cloud, Icons.Default.Schedule, Icons.Default.Settings)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VanSwift", fontSize = 20.sp)
                        Text(
                            "Semua Fitur Premium Terbuka (Gratis & Bebas Akun)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabTitles.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(tabIcons[index], contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> DashboardView()
                1 -> AppsListView()
                2 -> CloudSyncView()
                3 -> SchedulesView()
                4 -> SettingsView()
            }
        }
    }
}

@Composable
fun DashboardView() {
    val isRoot = remember { ShellExecutor.isRootAvailable() }
    val isShizuku = remember { ShellExecutor.isShizukuAvailable() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status Hak Akses", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Superuser (Root): ", fontSize = 14.sp)
                    Text(if (isRoot) "AKTIF ✓" else "TIDAK TERDETEKSI ✗", color = if (isRoot) Color.Green else Color.Yellow, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Shizuku (Adb Shell): ", fontSize = 14.sp)
                    Text(if (isShizuku) "AKTIF ✓" else "MENUNGGU IZIN ✗", color = if (isShizuku) Color.Green else Color.Yellow, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Pencadangan Cepat Data Sistem", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { /* Backup SMS */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SMS/MMS")
            }
            Button(
                onClick = { /* Backup Call Log */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Panggilan")
            }
            Button(
                onClick = { /* Backup Wi-Fi */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Sandi Wi-Fi")
            }
        }
    }
}

@Composable
fun AppsListView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daftar Aplikasi & Multi-Versi", style = MaterialTheme.typography.titleMedium)
        Text("Mendukung Split APKs, Data tar.gz, dan Proteksi Riwayat Cadangan", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Kelompok Label: Semua (Aplikasi Pengguna & Sistem)")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Run Batch Backup */ }) {
                    Text("Cadangkan Semua Aplikasi (Batch)")
                }
            }
        }
    }
}

@Composable
fun CloudSyncView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Penyimpanan Cloud Pribadi (Tanpa Akun Pihak Ketiga)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Pilih protokol cloud langsung ke server milik Anda sendiri:", fontSize = 13.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(12.dp))

        val protocols = listOf("WebDAV / Nextcloud / ownCloud", "SMB / Windows Network Share", "SFTP / SSH Server", "Amazon S3 / MinIO / Cloudflare R2")
        protocols.forEach { proto ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(proto, fontSize = 14.sp)
                    Button(onClick = { /* Setup Cloud */ }) {
                        Text("Hubungkan")
                    }
                }
            }
        }
    }
}

@Composable
fun SchedulesView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Jadwal Pencadangan Otomatis", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Jadwalkan backup berulang tanpa batasan:", fontSize = 13.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Jadwal Harian (Setiap 24 Jam)")
                Text("Kondisi: Hanya saat mengisi daya (Charging) + WiFi", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status: AKTIF (WorkManager System Service)", color = Color.Green, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun SettingsView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pengaturan VanSwift", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enkripsi Cadangan: AES-256 GCM (Lokal)", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tema: Pure AMOLED Dark Theme", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Multi-Threading: Unduhan & Unggahan Paralel Aktif", fontSize = 14.sp)
            }
        }
    }
}
