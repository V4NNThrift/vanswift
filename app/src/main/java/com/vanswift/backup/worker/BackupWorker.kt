package com.vanswift.backup.worker

import android.content.Context
import androidx.work.*
import com.vanswift.backup.engine.AppBackupEngine
import com.vanswift.backup.model.ScheduleRule
import java.io.File
import java.util.concurrent.TimeUnit

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val targetLabel = inputData.getString("target_label") ?: "Semua"
        val syncToCloud = inputData.getBoolean("sync_cloud", false)

        val outputBase = File(applicationContext.getExternalFilesDir(null), "VanSwift_Backups")
        val engine = AppBackupEngine(applicationContext)

        // Ambil daftar aplikasi terinstall
        val pm = applicationContext.packageManager
        val installed = pm.getInstalledApplications(0)

        for (app in installed) {
            // Filter hanya user app
            if ((app.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
                engine.backupApp(
                    packageName = app.packageName,
                    baseOutputDir = outputBase,
                    config = AppBackupEngine.BackupConfig(
                        backupApk = true,
                        backupData = true,
                        isProtected = false,
                        label = targetLabel
                    )
                )
            }
        }

        return Result.success()
    }

    companion object {
        fun registerSchedule(context: Context, rule: ScheduleRule) {
            val constraints = Constraints.Builder().apply {
                if (rule.requireCharging) setRequiresCharging(true)
                if (rule.requireWifiOnly) setRequiredNetworkType(NetworkType.UNMETERED)
                setRequiresBatteryNotLow(true)
            }.build()

            val data = Data.Builder()
                .putString("target_label", rule.targetLabel)
                .putBoolean("sync_cloud", rule.syncToCloud)
                .build()

            val request = PeriodicWorkRequestBuilder<BackupWorker>(rule.intervalHours, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "vanswift_schedule_${rule.id}",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelSchedule(context: Context, scheduleId: String) {
            WorkManager.getInstance(context).cancelUniqueWork("vanswift_schedule_$scheduleId")
        }
    }
}
