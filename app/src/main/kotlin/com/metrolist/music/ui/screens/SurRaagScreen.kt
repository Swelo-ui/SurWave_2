package com.metrolist.music.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metrolist.music.ui.theme.SurWaveBlack
import com.metrolist.music.ui.theme.SurWavePurple
import com.metrolist.music.ui.theme.SurWavePurpleLight
import java.util.Calendar

/**
 * Sur Raag Mode — recommends a classical Indian raag based on the time of day.
 * Naya screen, koi existing file modify nahi kiya.
 */
@Composable
fun SurRaagScreen() {
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
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SurWaveBlack, SurWavePurple.copy(alpha = 0.3f))
                )
            )
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Screen header
            Text(
                text = "🎵 Sur Raag Mode",
                style = MaterialTheme.typography.headlineMedium,
                color = SurWavePurpleLight,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Based on the current time of day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            SurRaagContent(raag = raag)
        }
    }
}

/**
 * Card displaying the recommended raag and its details.
 */
@Composable
fun SurRaagContent(raag: SurRaag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurWavePurple.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = raag.icon,
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = raag.name,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = raag.period,
                style = MaterialTheme.typography.titleMedium,
                color = SurWavePurpleLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = raag.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
