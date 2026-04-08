/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 *
 * Hand-crafted ColorScheme objects for SurWave's 5 curated music-personality themes.
 * These bypass MaterialKolor's tonal palette and use the exact user-specified colors,
 * giving vibrant/vivid results rather than muted/pastel tonal surfaces.
 */

package com.metrolist.music.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────────────────────────────────────
// Helper
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Builds a complete M3 ColorScheme from the curated theme colours.
 * We only specify the most impactful slots; less visible ones are
 * derived from the primary/surface values to keep things consistent.
 *
 * @param background    Main page background
 * @param surface       Card / bottom-sheet surface
 * @param primary       Accent / CTA colour
 * @param secondary     Secondary accent
 * @param onPrimary     Text/icon on primary colour
 * @param onBackground  Default body text colour
 * @param onSurface     Text on surface
 * @param onSurfaceVar  Subdued / caption text colour
 * @param surfaceVar    Slightly elevated surface (e.g. chips)
 * @param surfaceCont   Surface container (nav-bar, mini-player)
 * @param outline       Dividers / borders
 */
private fun buildScheme(
    background: Color,
    surface: Color,
    primary: Color,
    secondary: Color,
    onPrimary: Color,
    onBackground: Color,
    onSurface: Color,
    onSurfaceVar: Color,
    surfaceVar: Color,
    surfaceCont: Color,
    outline: Color,
): ColorScheme = ColorScheme(
    primary               = primary,
    onPrimary             = onPrimary,
    primaryContainer      = primary.copy(alpha = 0.22f).compositeOver(surface),
    onPrimaryContainer    = primary,

    secondary             = secondary,
    onSecondary           = onPrimary,
    secondaryContainer    = secondary.copy(alpha = 0.20f).compositeOver(surface),
    onSecondaryContainer  = secondary,

    tertiary              = secondary.copy(alpha = 0.75f).compositeOver(surface),
    onTertiary            = onPrimary,
    tertiaryContainer     = secondary.copy(alpha = 0.12f).compositeOver(surface),
    onTertiaryContainer   = secondary,

    error                 = Color(0xFFCF6679),
    onError               = Color(0xFF000000),
    errorContainer        = Color(0xFF93000A),
    onErrorContainer      = Color(0xFFFFDAD6),

    background            = background,
    onBackground          = onBackground,

    surface               = surface,
    onSurface             = onSurface,
    surfaceVariant        = surfaceVar,
    onSurfaceVariant      = onSurfaceVar,

    surfaceTint           = primary,
    inverseSurface        = onBackground,
    inverseOnSurface      = background,
    inversePrimary        = primary.copy(alpha = 0.80f).compositeOver(onBackground),

    outline               = outline,
    outlineVariant        = outline.copy(alpha = 0.35f).compositeOver(surface),

    scrim                 = Color(0xFF000000),

    surfaceBright         = surfaceVar,
    surfaceDim            = background,
    surfaceContainer      = surfaceCont,
    surfaceContainerHigh  = surfaceCont.copy(alpha = 0.85f).compositeOver(surface),
    surfaceContainerHighest = surfaceVar,
    surfaceContainerLow   = surface,
    surfaceContainerLowest = background,
)

/** Composite a translucent color over an opaque base. */
private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(
        red   = red   * a + background.red   * (1 - a),
        green = green * a + background.green * (1 - a),
        blue  = blue  * a + background.blue  * (1 - a),
        alpha = 1f,
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Theme 1 — Velvet Noir  (Purple · late-night / emotional)
// ──────────────────────────────────────────────────────────────────────────────

val VelvetNoirDark = buildScheme(
    background  = Color(0xFF0E0A14),
    surface     = Color(0xFF1E1530),
    primary     = Color(0xFFC084FC),
    secondary   = Color(0xFFF0ABFC),
    onPrimary   = Color(0xFF1A0533),
    onBackground = Color(0xFFFAF5FF),
    onSurface   = Color(0xFFFAF5FF),
    onSurfaceVar= Color(0xFFA78BCA),
    surfaceVar  = Color(0xFF2D1F47),
    surfaceCont = Color(0xFF2D1F47),
    outline     = Color(0xFF6B21A8).copy(alpha = 0.6f).compositeOver(Color(0xFF1E1530)),
)

val VelvetNoirLight = buildScheme(
    background  = Color(0xFFF7F3FF),
    surface     = Color(0xFFEDE6FF),
    primary     = Color(0xFF7E22CE),
    secondary   = Color(0xFFA855F7),
    onPrimary   = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A0533),
    onSurface   = Color(0xFF1A0533),
    onSurfaceVar= Color(0xFF6B21A8),
    surfaceVar  = Color(0xFFDDD6FE),
    surfaceCont = Color(0xFFDDD6FE),
    outline     = Color(0xFFA78BCA),
)

// ──────────────────────────────────────────────────────────────────────────────
// Theme 2 — Ember Pulse  (Orange-Red · gym / energetic)
// ──────────────────────────────────────────────────────────────────────────────

val EmberPulseDark = buildScheme(
    background  = Color(0xFF0F0500),
    surface     = Color(0xFF1F0A00),
    primary     = Color(0xFFFF4500),
    secondary   = Color(0xFFFF8C42),
    onPrimary   = Color(0xFF1A0800),
    onBackground = Color(0xFFFFF7F0),
    onSurface   = Color(0xFFFFF7F0),
    onSurfaceVar= Color(0xFFFF9966),
    surfaceVar  = Color(0xFF2E1000),
    surfaceCont = Color(0xFF2E1000),
    outline     = Color(0xFF9A3412).copy(alpha = 0.6f).compositeOver(Color(0xFF1F0A00)),
)

val EmberPulseLight = buildScheme(
    background  = Color(0xFFFFF8F0),
    surface     = Color(0xFFFFE8D0),
    primary     = Color(0xFFC2320A),
    secondary   = Color(0xFFEA580C),
    onPrimary   = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A0800),
    onSurface   = Color(0xFF1A0800),
    onSurfaceVar= Color(0xFF9A3412),
    surfaceVar  = Color(0xFFFFCBA4),
    surfaceCont = Color(0xFFFFCBA4),
    outline     = Color(0xFFFF9966),
)

// ──────────────────────────────────────────────────────────────────────────────
// Theme 3 — Aqua Soul  (Cyan · chill / creative)
// ──────────────────────────────────────────────────────────────────────────────

val AquaSoulDark = buildScheme(
    background  = Color(0xFF020B14),
    surface     = Color(0xFF041E30),
    primary     = Color(0xFF06B6D4),
    secondary   = Color(0xFF67E8F9),
    onPrimary   = Color(0xFF042330),
    onBackground = Color(0xFFECFEFF),
    onSurface   = Color(0xFFECFEFF),
    onSurfaceVar= Color(0xFF7DD3FC),
    surfaceVar  = Color(0xFF062A3F),
    surfaceCont = Color(0xFF062A3F),
    outline     = Color(0xFF075985).copy(alpha = 0.6f).compositeOver(Color(0xFF041E30)),
)

val AquaSoulLight = buildScheme(
    background  = Color(0xFFF0FBFF),
    surface     = Color(0xFFCFFAFE),
    primary     = Color(0xFF0369A1),
    secondary   = Color(0xFF0891B2),
    onPrimary   = Color(0xFFFFFFFF),
    onBackground = Color(0xFF042330),
    onSurface   = Color(0xFF042330),
    onSurfaceVar= Color(0xFF075985),
    surfaceVar  = Color(0xFFBAE6FD),
    surfaceCont = Color(0xFFBAE6FD),
    outline     = Color(0xFF7DD3FC),
)

// ──────────────────────────────────────────────────────────────────────────────
// Theme 4 — Forest Echo  (Green · nature / indie)
// ──────────────────────────────────────────────────────────────────────────────

val ForestEchoDark = buildScheme(
    background  = Color(0xFF030D06),
    surface     = Color(0xFF071A0C),
    primary     = Color(0xFF4ADE80),
    secondary   = Color(0xFF86EFAC),
    onPrimary   = Color(0xFF052E16),
    onBackground = Color(0xFFF0FFF4),
    onSurface   = Color(0xFFF0FFF4),
    onSurfaceVar= Color(0xFF6EE7A0),
    surfaceVar  = Color(0xFF0D2B15),
    surfaceCont = Color(0xFF0D2B15),
    outline     = Color(0xFF166534).copy(alpha = 0.6f).compositeOver(Color(0xFF071A0C)),
)

val ForestEchoLight = buildScheme(
    background  = Color(0xFFF1FDF4),
    surface     = Color(0xFFDCFCE7),
    primary     = Color(0xFF15803D),
    secondary   = Color(0xFF16A34A),
    onPrimary   = Color(0xFFFFFFFF),
    onBackground = Color(0xFF052E16),
    onSurface   = Color(0xFF052E16),
    onSurfaceVar= Color(0xFF166534),
    surfaceVar  = Color(0xFFBBF7D0),
    surfaceCont = Color(0xFFBBF7D0),
    outline     = Color(0xFF6EE7A0),
)

// ──────────────────────────────────────────────────────────────────────────────
// Theme 5 — Sakura Wave  (Rose-Red · romantic / emotional)
// ──────────────────────────────────────────────────────────────────────────────

val SakuraWaveDark = buildScheme(
    background  = Color(0xFF100508),
    surface     = Color(0xFF200B10),
    primary     = Color(0xFFFB7185),
    secondary   = Color(0xFFFDA4AF),
    onPrimary   = Color(0xFF1A0008),
    onBackground = Color(0xFFFFF1F2),
    onSurface   = Color(0xFFFFF1F2),
    onSurfaceVar= Color(0xFFFCA5A5),
    surfaceVar  = Color(0xFF2D0A12),
    surfaceCont = Color(0xFF2D0A12),
    outline     = Color(0xFF9F1239).copy(alpha = 0.6f).compositeOver(Color(0xFF200B10)),
)

val SakuraWaveLight = buildScheme(
    background  = Color(0xFFFFF5F7),
    surface     = Color(0xFFFFE4EA),
    primary     = Color(0xFFBE123C),
    secondary   = Color(0xFFE11D48),
    onPrimary   = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A0008),
    onSurface   = Color(0xFF1A0008),
    onSurfaceVar= Color(0xFF9F1239),
    surfaceVar  = Color(0xFFFECDD3),
    surfaceCont = Color(0xFFFECDD3),
    outline     = Color(0xFFFCA5A5),
)

// ──────────────────────────────────────────────────────────────────────────────
// Registry — map theme key to dark/light pair
// ──────────────────────────────────────────────────────────────────────────────

/** Returns (darkScheme, lightScheme) for a curated theme key, or null if not found. */
fun getCuratedColorScheme(key: String): Pair<ColorScheme, ColorScheme>? =
    when (key) {
        "velvet_noir"  -> VelvetNoirDark  to VelvetNoirLight
        "ember_pulse"  -> EmberPulseDark  to EmberPulseLight
        "aqua_soul"    -> AquaSoulDark    to AquaSoulLight
        "forest_echo"  -> ForestEchoDark  to ForestEchoLight
        "sakura_wave"  -> SakuraWaveDark  to SakuraWaveLight
        else           -> null
    }
