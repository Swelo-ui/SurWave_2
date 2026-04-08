/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

/** Progress state for a download */
sealed class DownloadState {
    object Idle : DownloadState()
    data class Downloading(val progress: Float, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    object Installing : DownloadState()
    data class Failed(val reason: String) : DownloadState()
    object Success : DownloadState()
}

object ApkInstaller {

    private const val APK_FILE_NAME = "SurWave_update.apk"

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    /**
     * Downloads an APK from [url] fully in-app using OkHttp (no browser, no DownloadManager).
     * Emits [DownloadState] progress updates and triggers the system install dialog on completion.
     */
    fun downloadAndInstall(context: Context, url: String): Flow<DownloadState> = flow {
        emit(DownloadState.Downloading(0f, 0L, 0L))

        val destFile = File(context.cacheDir, APK_FILE_NAME)
        if (destFile.exists()) destFile.delete()

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "SurWave-UpdaterApp")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                emit(DownloadState.Failed("Server returned ${response.code}"))
                return@flow
            }

            val body = response.body ?: run {
                emit(DownloadState.Failed("Empty response body"))
                return@flow
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            destFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        val progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
                        emit(DownloadState.Downloading(progress, downloadedBytes, totalBytes))
                    }
                }
            }

            emit(DownloadState.Installing)
            installApk(context, destFile)
            emit(DownloadState.Success)

        } catch (e: Exception) {
            destFile.delete()
            emit(DownloadState.Failed(e.message ?: "Download failed"))
        }
    }.flowOn(Dispatchers.IO)

    /** Trigger APK install using FileProvider so Android shows the in-app install dialog */
    private fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.FileProvider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }
}
