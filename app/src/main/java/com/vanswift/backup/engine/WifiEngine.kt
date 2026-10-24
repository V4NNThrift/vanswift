package com.vanswift.backup.engine

import com.vanswift.backup.core.privilege.ShellExecutor
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class WifiEngine {

    fun backupWifiConfigs(outputFile: File): Result<Int> = runCatching {
        // Path standar konfigurasi WiFi di Android (Root privilege)
        val paths = listOf(
            "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml",
            "/data/misc/wifi/WifiConfigStore.xml",
            "/data/misc/wifi/wpa_supplicant.conf"
        )

        var foundContent = ""
        for (path in paths) {
            val res = ShellExecutor.execute("cat $path", preferRoot = true)
            if (res.isSuccess && res.stdout.isNotEmpty()) {
                foundContent = res.stdout
                break
            }
        }

        if (foundContent.isEmpty()) {
            throw RuntimeException("Gagal membaca konfigurasi WiFi. Akses Root diperlukan.")
        }

        outputFile.writeText(foundContent)
        1
    }

    fun restoreWifiConfigs(inputFile: File): Result<Boolean> = runCatching {
        val targetPath = "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml"
        val cmd = "cp ${inputFile.absolutePath} $targetPath && chmod 600 $targetPath && chown wifi:wifi $targetPath"
        val res = ShellExecutor.execute(cmd, preferRoot = true)
        res.isSuccess
    }
}
