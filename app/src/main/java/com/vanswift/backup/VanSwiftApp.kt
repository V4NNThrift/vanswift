package com.vanswift.backup

import android.app.Application
import android.os.Build
import rikka.shizuku.Shizuku

class VanSwiftApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Inisialisasi listener Shizuku
        try {
            Shizuku.addRequestPermissionResultListener { _, grantResult ->
                isShizukuGranted = (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)
            }
        } catch (_: Throwable) {}
    }

    companion object {
        lateinit var instance: VanSwiftApp
            private set

        var isShizukuGranted: Boolean = false
        var isRootAvailable: Boolean = false
    }
}
