package com.vanswift.backup.core.privilege

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShellExecutor {

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = BufferedReader(InputStreamReader(process.inputStream)).readLine()
            process.waitFor()
            output != null && output.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun execute(command: String, preferRoot: Boolean = true): CommandResult {
        // 1. Coba lewat Root Superuser jika preferRoot atau Shizuku tidak ada
        if (preferRoot || !isShizukuAvailable()) {
            try {
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
                val stdout = process.inputStream.bufferedReader().use { it.readText() }
                val stderr = process.errorStream.bufferedReader().use { it.readText() }
                val exitCode = process.waitFor()
                if (exitCode == 0) {
                    return CommandResult(exitCode, stdout, stderr)
                }
            } catch (_: Exception) {}
        }

        // 2. Fallback ke standard runtime exec
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            CommandResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            CommandResult(-1, "", e.localizedMessage ?: "Execution failed")
        }
    }

    data class CommandResult(val exitCode: Int, val stdout: String, val stderr: String) {
        val isSuccess: Boolean get() = exitCode == 0
    }
}
