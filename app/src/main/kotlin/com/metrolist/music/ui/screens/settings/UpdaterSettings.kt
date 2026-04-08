/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.BuildConfig
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.CheckForUpdatesKey
import com.metrolist.music.constants.UpdateNotificationsEnabledKey
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.ApkInstaller
import com.metrolist.music.utils.DownloadState
import com.metrolist.music.utils.Updater
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(
    navController: NavController
) {
    val (checkForUpdates, onCheckForUpdatesChange) = rememberPreference(CheckForUpdatesKey, true)
    val (updateNotifications, onUpdateNotificationsChange) = rememberPreference(UpdateNotificationsEnabledKey, true)

    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf(false) }
    var latestVersion by remember { mutableStateOf<String?>(null) }
    var downloadUrl by remember { mutableStateOf<String?>(null) }
    var showChangelog by remember { mutableStateOf(false) }
    var changelogContent by remember { mutableStateOf<String?>(null) }
    var checkError by remember { mutableStateOf<String?>(null) }

    // Download state
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }

    val failedToCheckUpdatesTemplate = stringResource(R.string.failed_to_check_updates)
    val coroutineScope = rememberCoroutineScope()

    fun performManualCheck() {
        coroutineScope.launch {
            isChecking = true
            checkError = null
            withContext(Dispatchers.IO) {
                Updater
                    .checkForUpdate(forceRefresh = true)
                    .onSuccess { (releaseInfo, hasUpdate) ->
                        if (releaseInfo != null) {
                            latestVersion = releaseInfo.versionName
                            updateAvailable = hasUpdate
                            changelogContent = releaseInfo.description
                            downloadUrl = Updater.getDownloadUrlForCurrentVariant(releaseInfo)
                        }
                    }.onFailure {
                        checkError = String.format(failedToCheckUpdatesTemplate, it.message ?: "Unknown error")
                    }
            }
            isChecking = false
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    LocalPlayerAwareWindowInsets.current.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                ).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top,
                ),
            ),
        )

        Spacer(Modifier.height(4.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.current_version),
            items =
                listOf(
                    Material3SettingsItem(
                        title = {
                            Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME))
                        },
                        description = {
                            val arch = BuildConfig.ARCHITECTURE
                            val variant = if (BuildConfig.CAST_AVAILABLE) "GMS" else "FOSS"
                            Text("$arch - $variant")
                        },
                    ),
                ),
        )

        Spacer(Modifier.height(16.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.update_settings),
            items =
                buildList {
                    add(
                        Material3SettingsItem(
                            title = { Text(stringResource(R.string.check_for_updates)) },
                            icon = painterResource(R.drawable.update),
                            trailingContent = {
                                Switch(
                                    checked = checkForUpdates,
                                    onCheckedChange = onCheckForUpdatesChange,
                                )
                            },
                            onClick = { onCheckForUpdatesChange(!checkForUpdates) },
                        ),
                    )

                    if (checkForUpdates) {
                        add(
                            Material3SettingsItem(
                                title = { Text(stringResource(R.string.update_notifications)) },
                                icon = painterResource(R.drawable.notification),
                                trailingContent = {
                                    Switch(
                                        checked = updateNotifications,
                                        onCheckedChange = onUpdateNotificationsChange,
                                    )
                                },
                                onClick = { onUpdateNotificationsChange(!updateNotifications) },
                            ),
                        )
                    }
                },
        )

        Spacer(Modifier.height(16.dp))

        Material3SettingsGroup(
            title = stringResource(R.string.check_for_updates_title),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.refresh),
                        title = {
                            if (isChecking) {
                                Text(stringResource(R.string.checking_for_updates))
                            } else if (latestVersion != null) {
                                Text(stringResource(R.string.latest_version_format, latestVersion!!))
                            } else {
                                Text(stringResource(R.string.check_for_updates_button))
                            }
                        },
                        trailingContent = {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(end = 16.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else if (updateAvailable) {
                                Icon(
                                    painter = painterResource(R.drawable.download),
                                    contentDescription = stringResource(R.string.update_available_title),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                        onClick = { if (!isChecking) performManualCheck() },
                    ),
                ),
        )

        checkError?.let {
            Spacer(Modifier.height(12.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        // ── Update available panel ──────────────────────────────────────────
        AnimatedVisibility(
            visible = updateAvailable && latestVersion != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(16.dp))

                // ── Download / progress card ────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SurWave $latestVersion ${stringResource(R.string.update_available_title)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(Modifier.height(16.dp))

                        when (val state = downloadState) {
                            is DownloadState.Idle -> {
                                // Show download button only if URL is available
                                if (downloadUrl != null) {
                                    Button(
                                        onClick = {
                                            val url = downloadUrl ?: return@Button
                                            coroutineScope.launch {
                                                ApkInstaller.downloadAndInstall(context, url)
                                                    .collect { downloadState = it }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.download),
                                            contentDescription = null,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(stringResource(R.string.download_update))
                                    }
                                }
                            }

                            is DownloadState.Downloading -> {
                                val animatedProgress by animateFloatAsState(
                                    targetValue = state.progress,
                                    animationSpec = tween(300),
                                    label = "download_progress"
                                )
                                Text(
                                    text = if (state.totalBytes > 0) {
                                        "${(state.downloadedBytes / 1024 / 1024)}MB / ${(state.totalBytes / 1024 / 1024)}MB"
                                    } else {
                                        stringResource(R.string.checking_for_updates)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${(state.progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            is DownloadState.Installing -> {
                                Text(
                                    text = "Installing… Please accept the prompt.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }

                            is DownloadState.Success -> {
                                Text(
                                    text = "Update ready! Complete the install from the system prompt.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            is DownloadState.Failed -> {
                                Text(
                                    text = "Download failed: ${state.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        downloadState = DownloadState.Idle
                                        val url = downloadUrl ?: return@Button
                                        coroutineScope.launch {
                                            ApkInstaller.downloadAndInstall(context, url)
                                                .collect { downloadState = it }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Retry Download") }
                            }
                        }
                    }
                }

                // ── Changelog toggle ────────────────────────────────────────
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { showChangelog = !showChangelog },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Text(if (showChangelog) stringResource(R.string.hide_changelog) else stringResource(R.string.view_changelog))
                }

                if (showChangelog && changelogContent != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            MarkdownText(changelogContent!!)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.updater)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}
