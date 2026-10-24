package com.vanswift.backup.core.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class WebDavClient(
    private val hostUrl: String,
    private val username: String,
    private val password: String
) {
    private val client = OkHttpClient.Builder()
        .authenticator { _, response ->
            if (response.request.header("Authorization") != null) return@authenticator null
            val credential = Credentials.basic(username, password)
            response.request.newBuilder().header("Authorization", credential).build()
        }
        .build()

    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val req = Request.Builder()
                .url(hostUrl)
                .method("PROPFIND", null)
                .build()
            client.newCall(req).execute().use { resp ->
                resp.isSuccessful || resp.code in 200..299 || resp.code == 405
            }
        }
    }

    suspend fun uploadFile(localFile: File, remoteRelativePath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "${hostUrl.trimEnd('/')}/${remoteRelativePath.trimStart('/')}"
            val body = localFile.asRequestBody()
            val req = Request.Builder()
                .url(targetUrl)
                .put(body)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("Upload failed with HTTP ${resp.code}")
                true
            }
        }
    }

    suspend fun downloadFile(remoteRelativePath: String, targetLocalFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val targetUrl = "${hostUrl.trimEnd('/')}/${remoteRelativePath.trimStart('/')}"
            val req = Request.Builder()
                .url(targetUrl)
                .get()
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("Download failed with HTTP ${resp.code}")
                resp.body?.byteStream()?.use { input ->
                    FileOutputStream(targetLocalFile).use { output ->
                        input.copyTo(output)
                    }
                }
                true
            }
        }
    }
}
