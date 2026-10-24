package com.vanswift.backup.model

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val apkSize: Long,
    val dataSize: Long,
    val icon: Drawable? = null,
    val label: String = "Default",
    val isProtected: Boolean = false,
    val backupVersionsCount: Int = 0
)

data class BackupMetadata(
    val packageName: String,
    val appName: String,
    val versionCode: Long,
    val versionName: String,
    val timestamp: String,
    val hasApk: Boolean,
    val hasData: Boolean,
    val isProtected: Boolean,
    val label: String
)

data class ScheduleRule(
    val id: String,
    val title: String,
    val targetLabel: String, // e.g. "Semua", "Game", "Finansial"
    val intervalHours: Long,
    val requireCharging: Boolean,
    val requireWifiOnly: Boolean,
    val syncToCloud: Boolean,
    val isEnabled: Boolean
)
