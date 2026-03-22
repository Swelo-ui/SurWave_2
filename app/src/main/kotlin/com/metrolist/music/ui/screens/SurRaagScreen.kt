package com.metrolist.music.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.metrolist.innertube.YouTube
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.LocalPlayerConnection
import com.metrolist.music.R
import com.metrolist.music.extensions.toMediaItem
import com.metrolist.music.playback.queues.ListQueue
import com.metrolist.music.ui.theme.SurWaveBlack
import com.metrolist.music.ui.theme.SurWavePurple
import com.metrolist.music.ui.theme.SurWavePurpleLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.ui.component.ChipsRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

enum class SurSpecialTab(val title: String) {
    RAAG_CLOCK("Raag Clock"),
    RADHARANI_SPECIAL("RadhaRani Special"),
    SUR_RADAR("SurRadar")
}

/**
 * Sur Special Main Screen - 3 tabs: Raag Clock, RadhaRani Special, SurRadar
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SurRaagScreen(navController: NavController) {
    val pagerState = rememberPagerState(pageCount = { SurSpecialTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    
    val playerAwareWindowInsets = LocalPlayerAwareWindowInsets.current
    val insets = playerAwareWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
    
    var selectedTab by remember { mutableStateOf(SurSpecialTab.RAAG_CLOCK) }
    
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = SurSpecialTab.entries.getOrNull(pagerState.currentPage) ?: SurSpecialTab.RAAG_CLOCK
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(insets.asPaddingValues())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ChipsRow(
                chips = SurSpecialTab.entries.map { it to it.title },
                currentValue = selectedTab,
                onValueUpdate = {
                    selectedTab = it
                    coroutineScope.launch { pagerState.animateScrollToPage(it.ordinal) }
                },
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> RaagClockTab()
                1 -> RadhaRaniSpecialTab()
                2 -> SurRadarTab(navController)
            }
        }
    }
}

@Composable
fun RaagClockTab() {
    val raag = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        getSurRaagForHour(hour)
    }

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "fadeIn"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha),
        contentAlignment = Alignment.TopCenter
    ) {
        // A subtle background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(500f, 0f),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Based on the current time of day",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            SurRaagContent(raag = raag)
        }
    }
}

@Composable
fun RadhaRaniSpecialTab() {
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // A subtle background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(500f, 0f),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Exclusive Bhajans & Kirtans",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.secondaryContainer, Color.Transparent)
                            )
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "🦚",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Shri Radha",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Divine Playlist",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Dive into deep devotion with a hand-picked, randomly shuffled exclusive mix of Shri Radha Rani bhajans.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val queries = listOf(
                                        "Radha Rani Bhajan",
                                        "Shri Radha Kirtan",
                                        "Kishori Kuch Aisa Intezam",
                                        "Radhe Radhe Barsane Wali Radhe",
                                        "Shri Radha Sahasranama",
                                        "Radha Kirpa Katakhsa",
                                        "Radha Krishna Bhajan",
                                        "Braj Gopi Bhajan"
                                    )
                                    val randomQuery = queries.random()
                                    val result = YouTube.search(randomQuery, YouTube.SearchFilter.FILTER_SONG).getOrNull()
                                    val songs = result?.items?.filterIsInstance<SongItem>()?.map { it.toMediaItem() }?.shuffled()
                                    if (!songs.isNullOrEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            playerConnection?.playQueue(
                                                ListQueue(
                                                    title = "RadhaRani Special",
                                                    items = songs
                                                )
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Play Special Mix",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurRadarTab(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // A subtle background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(500f, 0f),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Discover Music Around You",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("recognition") }
                    .animateContentSize(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.tertiaryContainer, Color.Transparent)
                            )
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mic),
                                contentDescription = "SurRadar Mic",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "SurRadar",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.ExtraBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Audio Recognition",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Let SurWave listen and identify the song playing around you instantly with high accuracy.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Button(
                            onClick = {
                                navController.navigate("recognition")
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.mic),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Start Listening",
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card displaying the recommended raag and its details.
 */
@Composable
fun SurRaagContent(raag: SurRaag) {
    val playerConnection = LocalPlayerConnection.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Inner gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            Color.Transparent
                        )
                    )
                )
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = raag.icon,
                    fontSize = 64.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = raag.name,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = raag.period,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = raag.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val result = YouTube.search("${raag.name} Raag", YouTube.SearchFilter.FILTER_SONG).getOrNull()
                            val songs = result?.items?.filterIsInstance<SongItem>()?.map { it.toMediaItem() }
                            if (!songs.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    playerConnection?.playQueue(
                                        ListQueue(
                                            title = "${raag.name} Special",
                                            items = songs
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Play ${raag.name}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Data class representing a Sur Raag recommendation.
 */
data class SurRaag(
    val name: String,
    val period: String,
    val description: String,
    val icon: String
)

/**
 * Returns the appropriate raag for a given hour of the day.
 * Based on traditional Hindustani classical music timing conventions.
 */
fun getSurRaagForHour(hour: Int): SurRaag = when (hour) {
    in 4..6 -> SurRaag(
        name = "Bhairav",
        period = "Prabhat · Dawn (4–6 AM)",
        description = "A serene morning raag that evokes the calm and purity of dawn. Perfect for meditation and new beginnings.",
        icon = "🌅"
    )
    in 6..10 -> SurRaag(
        name = "Bhoopali",
        period = "Subah · Morning (6–10 AM)",
        description = "A bright and joyful raag that captures the freshness of the morning. Full of optimism and energy.",
        icon = "☀️"
    )
    in 10..14 -> SurRaag(
        name = "Bilawal",
        period = "Dopahar · Afternoon (10 AM–2 PM)",
        description = "A clear and uplifting raag suited for the high sun hours. Conveys a sense of clarity and purpose.",
        icon = "🌞"
    )
    in 14..18 -> SurRaag(
        name = "Bhimpalasi",
        period = "Sham · Evening (2–6 PM)",
        description = "A deeply emotive raag of the late afternoon, rich with longing and introspection.",
        icon = "🌤️"
    )
    in 18..21 -> SurRaag(
        name = "Yaman",
        period = "Sandhya · Dusk (6–9 PM)",
        description = "One of the most beloved evening raags in Hindustani music. Romantic, reflective, and beautifully expansive.",
        icon = "🌆"
    )
    else -> SurRaag(
        name = "Bhairavi",
        period = "Raat · Night (9 PM–4 AM)",
        description = "The quintessential concluding raag, deep and meditative. It brings a sense of closure and peace.",
        icon = "🌙"
    )
}
