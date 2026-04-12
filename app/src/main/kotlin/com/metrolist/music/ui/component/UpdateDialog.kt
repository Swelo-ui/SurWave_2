/**
 * SurWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.metrolist.music.R
import com.metrolist.music.ui.screens.settings.MarkdownText
import com.metrolist.music.utils.ApkInstaller
import com.metrolist.music.utils.DownloadState
import com.metrolist.music.utils.ReleaseInfo
import kotlinx.coroutines.launch

/**
 * A full in-app update dialog that:
 * - Displays the release version and date
 * - Shows a formatted Markdown changelog
 * - Downloads the APK in-app with a live progress bar
 * - Triggers the system package installer once download is complete
 *
 * @param releaseInfo   Data for the latest available release from GitHub
 * @param downloadUrl   Direct download URL of the APK for the current device variant (may be null)
 * @param onDismiss     Called when the user clicks "Later" or the dialog is dismissed
 */
@Composable
fun UpdateDialog(
    releaseInfo: ReleaseInfo,
    downloadUrl: String?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var showChangelog by remember { mutableStateOf(false) }

    val isDownloading = downloadState is DownloadState.Downloading
    val isInstalling = downloadState is DownloadState.Installing
    val isSuccess = downloadState is DownloadState.Success
    val isFailed = downloadState is DownloadState.Failed

    // Progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = (downloadState as? DownloadState.Downloading)?.progress ?: 0f,
        animationSpec = tween(300),
        label = "update_download_progress",
    )

    Dialog(
        onDismissRequest = {
            // Prevent dismissal while downloading
            if (!isDownloading && !isInstalling) onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = !isDownloading && !isInstalling,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                // Header: Update icon + title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.update),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.update_available_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "SurWave ${releaseInfo.versionName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Version pill + date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape,
                    ) {
                        Text(
                            text = releaseInfo.tagName,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Text(
                        text = releaseInfo.releaseDate.split("T").firstOrNull() ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Download progress section ───────────────────────────────
                AnimatedVisibility(
                    visible = isDownloading || isInstalling || isSuccess || isFailed,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (val state = downloadState) {
                            is DownloadState.Downloading -> {
                                val downloaded = state.downloadedBytes / 1024 / 1024
                                val total = state.totalBytes / 1024 / 1024
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = if (total > 0) "${downloaded}MB / ${total}MB"
                                        else stringResource(R.string.checking_for_updates),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "${(state.progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            is DownloadState.Installing -> {
                                Text(
                                    text = "Installing… Please accept the system prompt.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(12.dp))
                            }
                            is DownloadState.Success -> {
                                Text(
                                    text = "Download complete! Complete the install from the system prompt.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            is DownloadState.Failed -> {
                                Text(
                                    text = "Download failed: ${state.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            else -> {}
                        }
                    }
                }

                // ── Changelog toggle ────────────────────────────────────────
                TextButton(
                    onClick = { showChangelog = !showChangelog },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = if (showChangelog) stringResource(R.string.hide_changelog)
                        else stringResource(R.string.view_changelog),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }

                AnimatedVisibility(
                    visible = showChangelog,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MarkdownText(text = releaseInfo.description)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Action buttons ──────────────────────────────────────────
                if (!isDownloading && !isInstalling && !isSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // "Later" button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            enabled = !isDownloading,
                        ) {
                            Text(stringResource(R.string.cancel))
                        }

                        // "Update" button (only when downloadUrl is available)
                        if (downloadUrl != null && !isFailed) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        ApkInstaller.downloadAndInstall(context, downloadUrl)
                                            .collect { state -> downloadState = state }
                                    }
                                },
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.download),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .size(18.dp),
                                )
                                Text(stringResource(R.string.download_update))
                            }
                        } else if (isFailed) {
                            Button(
                                onClick = {
                                    downloadState = DownloadState.Idle
                                    val url = downloadUrl ?: return@Button
                                    coroutineScope.launch {
                                        ApkInstaller.downloadAndInstall(context, url)
                                            .collect { state -> downloadState = state }
                                    }
                                },
                                modifier = Modifier.weight(2f),
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (isSuccess) {
                    // Success: only show dismiss
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
