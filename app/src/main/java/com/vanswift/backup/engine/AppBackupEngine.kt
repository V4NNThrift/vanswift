package com.vanswift.backup.engine

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.vanswift.backup.core.privilege.ShellExecutor
import com.vanswift.backup.model.BackupMetadata
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppBackupEngine(private val context: Context) {

    private val pm: PackageManager = context.packageManager

    data class BackupConfig(
        val backupApk: Boolean = true,
        val backupData: Boolean = true,
        val isProtected: Boolean = false,
        val label: String = "Default"
    )

    fun backupApp(
        packageName: String,
        baseOutputDir: File,
        config: BackupConfig
    ): Result<File> = runCatching {
        val appInfo = pm.getApplicationInfo(packageName, 0)
        val pInfo = pm.getPackageInfo(packageName, 0)
        val appName = pm.getApplicationLabel(appInfo).toString()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        val appDir = File(baseOutputDir, "apps/$packageName/$timestamp").apply { mkdirs() }

        // 1. Backup Base APK & Splits
        if (config.backupApk) {
            val baseApk = File(appInfo.sourceDir)
            baseApk.copyTo(File(appDir, "base.apk"), overwrite = true)

            appInfo.splitSourceDirs?.forEachIndexed { idx, path ->
                val split = File(path)
                split.copyTo(File(appDir, "split_${split.name}"), overwrite = true)
            }
        }

        // 2. Backup Internal Data (via su tar)
        if (config.backupData) {
            val dataArchive = File(appDir, "data.tar.gz")
            val cmd = "tar -czf ${dataArchive.absolutePath} -C /data/data/$packageName ."
            val res = ShellExecutor.execute(cmd, preferRoot = true)
            if (!res.isSuccess) {
                // Warning jika gagal izin
                File(appDir, "data_error.log").writeText(res.stderr)
            }
        }

        // 3. Tulis Metadata Versi
        val meta = BackupMetadata(
            packageName = packageName,
            appName = appName,
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else @Suppress("DEPRECATION") pInfo.versionCode.toLong(),
            versionName = pInfo.versionName ?: "1.0",
            timestamp = timestamp,
            hasApk = config.backupApk,
            hasData = config.backupData,
            isProtected = config.isProtected,
            label = config.label
        )

        val json = JSONObject().apply {
            put("packageName", meta.packageName)
            put("appName", meta.appName)
            put("versionCode", meta.versionCode)
            put("versionName", meta.versionName)
            put("timestamp", meta.timestamp)
            put("hasApk", meta.hasApk)
            put("hasData", meta.hasData)
            put("isProtected", meta.isProtected)
            put("label", meta.label)
        }
        File(appDir, "meta.json").writeText(json.toString(2))

        appDir
    }

    fun restoreApp(backupDir: File): Result<Boolean> = runCatching {
        val baseApk = File(backupDir, "base.apk")
        val dataArchive = File(backupDir, "data.tar.gz")

        // 1. Install APK (Root / Shizuku / Standard PM)
        if (baseApk.exists()) {
            val splits = backupDir.listFiles { _, name -> name.startsWith("split_") && name.endsWith(".apk") } ?: emptyArray()
            if (splits.isEmpty()) {
                ShellExecutor.execute("pm install -r ${baseApk.absolutePath}")
            } else {
                // Install Split APKs batch session
                val sessionRes = ShellExecutor.execute("pm install-create -r")
                val sessionId = sessionRes.stdout.filter { it.isDigit() }
                if (sessionId.isNotEmpty()) {
                    ShellExecutor.execute("pm install-write -S ${baseApk.length()} $sessionId base ${baseApk.absolutePath}")
                    splits.forEachIndexed { i, split ->
                        ShellExecutor.execute("pm install-write -S ${split.length()} $sessionId split_$i ${split.absolutePath}")
                    }
                    ShellExecutor.execute("pm install-commit $sessionId")
                }
            }
        }

        // 2. Restore Data (tar extract)
        if (dataArchive.exists()) {
            val metaFile = File(backupDir, "meta.json")
            val pkg = if (metaFile.exists()) JSONObject(metaFile.readText()).optString("packageName") else backupDir.parentFile?.name
            if (!pkg.isNullOrEmpty()) {
                ShellExecutor.execute("tar -xzf ${dataArchive.absolutePath} -C /data/data/$pkg")
                ShellExecutor.execute("chown -R $pkg:$pkg /data/data/$pkg")
            }
        }

        true
    }
}
