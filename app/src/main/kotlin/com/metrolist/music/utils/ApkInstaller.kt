/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
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

    /**
     * Downloads an APK from [url] and emits [DownloadState] progress updates.
     * When complete, automatically triggers the system install dialog.
     */
    fun downloadAndInstall(context: Context, url: String): Flow<DownloadState> = callbackFlow {
        trySend(DownloadState.Downloading(0f, 0L, 0L))

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Clean up any previous update APK
        val destFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            APK_FILE_NAME
        )
        if (destFile.exists()) destFile.delete()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("SurWave Update")
            .setDescription("Downloading new version…")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setMimeType("application/vnd.android.package-archive")

        val downloadId = dm.enqueue(request)

        // Completion receiver
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id != downloadId) return

                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = if (statusCol >= 0) cursor.getInt(statusCol) else -1
                    cursor.close()

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        trySend(DownloadState.Installing)
                        installApk(ctx, destFile)
                        trySend(DownloadState.Success)
                    } else {
                        trySend(DownloadState.Failed("Download failed (status=$status)"))
                    }
                } else {
                    cursor?.close()
                    trySend(DownloadState.Failed("Could not query download status"))
                }
                close()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        // Progress polling loop - runs within the callbackFlow's coroutine scope
        val progressJob = launch(Dispatchers.IO) {
            while (true) {
                delay(500)
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query) ?: continue
                if (!cursor.moveToFirst()) { cursor.close(); continue }

                val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val downloadedCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                val status = if (statusCol >= 0) cursor.getInt(statusCol) else -1
                val downloaded = if (downloadedCol >= 0) cursor.getLong(downloadedCol) else 0L
                val total = if (totalCol >= 0) cursor.getLong(totalCol) else -1L
                cursor.close()

                when (status) {
                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED -> {
                        val progress = if (total > 0) downloaded.toFloat() / total else 0f
                        trySend(DownloadState.Downloading(progress, downloaded, total))
                    }
                    DownloadManager.STATUS_FAILED -> {
                        trySend(DownloadState.Failed("Download failed"))
                        close()
                        break
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> break
                }
            }
        }

        awaitClose {
            progressJob.cancel()
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
            // Cancel download if flow is cancelled before completion
            dm.remove(downloadId)
        }
    }

    /** Trigger APK install using FileProvider so Android shows the install dialog */
    private fun installApk(context: Context, apkFile: File) {
        try {
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
        } catch (e: Exception) {
            // Fallback: open in browser if install fails
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Swelo-ui/SurWave_2/releases/latest"))
            browserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(browserIntent)
        }
    }
}
