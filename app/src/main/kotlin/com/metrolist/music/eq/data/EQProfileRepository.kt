package com.metrolist.music.eq.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Saved EQ Profile with metadata
 */
@Serializable
data class SavedEQProfile(
    val id: String,                       // Unique identifier
    val name: String,                     // Display name
    val deviceModel: String,              // e.g., "Sony WH-1000XM4"
    val bands: List<ParametricEQBand>,    // EQ bands
    val preamp: Double = 0.0,             // Preamp gain in dB
    val isCustom: Boolean = false,        // Whether this is a custom imported profile
    val isActive: Boolean = false,        // Whether this profile is currently active
    val addedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Repository for managing EQ profiles
 * Handles saving, loading, and activating EQ profiles
 */
@Singleton
class EQProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "nanosonic_eq_profiles",
        Context.MODE_PRIVATE
    )

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val _profiles = MutableStateFlow<List<SavedEQProfile>>(emptyList())
    val profiles: StateFlow<List<SavedEQProfile>> = _profiles.asStateFlow()

    private val _activeProfile = MutableStateFlow<SavedEQProfile?>(null)
    val activeProfile: StateFlow<SavedEQProfile?> = _activeProfile.asStateFlow()

    companion object {
        private const val KEY_PROFILES = "eq_profiles"
        private const val KEY_ACTIVE_PROFILE_ID = "active_profile_id"
    }

    init {
        loadProfiles()
    }

    /**
     * Load all saved profiles from SharedPreferences
     */
    private fun loadProfiles() {
        try {
            val profilesJson = prefs.getString(KEY_PROFILES, null)
            if (profilesJson != null) {
                val loadedProfiles = json.decodeFromString<List<SavedEQProfile>>(profilesJson)
                _profiles.value = loadedProfiles

                // Load active profile
                val activeId = prefs.getString(KEY_ACTIVE_PROFILE_ID, null)
                _activeProfile.value = loadedProfiles.find { it.id == activeId }
            } else {
                seedInitialPresets()
            }
        } catch (e: Exception) {
            println("Error loading EQ profiles: ${e.message}")
            _profiles.value = emptyList()
            _activeProfile.value = null
        }
    }

    /**
     * Save a new EQ profile
     */
    suspend fun saveProfile(profile: SavedEQProfile) = withContext(Dispatchers.IO) {
        val currentProfiles = _profiles.value.toMutableList()

        // Check if profile with same ID already exists
        val existingIndex = currentProfiles.indexOfFirst { it.id == profile.id }

        if (existingIndex >= 0) {
            // Update existing profile
            currentProfiles[existingIndex] = profile
        } else {
            // Add new profile
            currentProfiles.add(profile)
        }

        // Save to SharedPreferences
        val profilesJson = json.encodeToString<List<SavedEQProfile>>(currentProfiles)
        prefs.edit { putString(KEY_PROFILES, profilesJson) }

        _profiles.value = currentProfiles
    }

    /**
     * Delete a profile
     */
    suspend fun deleteProfile(profileId: String) = withContext(Dispatchers.IO) {
        val currentProfiles = _profiles.value.toMutableList()
        currentProfiles.removeAll { it.id == profileId }

        val profilesJson = json.encodeToString<List<SavedEQProfile>>(currentProfiles)
        prefs.edit { putString(KEY_PROFILES, profilesJson) }

        // If deleted profile was active, clear active profile
        if (_activeProfile.value?.id == profileId) {
            _activeProfile.value = null
            prefs.edit { remove(KEY_ACTIVE_PROFILE_ID) }
        }

        _profiles.value = currentProfiles
    }

    /**
     * Set a profile as active (only one profile can be active at a time)
     * Pass null to deactivate all profiles
     */
    suspend fun setActiveProfile(profileId: String?) = withContext(Dispatchers.IO) {
        val currentProfiles = _profiles.value

        if (profileId == null) {
            // Deactivate all profiles
            _activeProfile.value = null
            prefs.edit { remove(KEY_ACTIVE_PROFILE_ID) }
        } else {
            val profile = currentProfiles.find { it.id == profileId }
            _activeProfile.value = profile
            prefs.edit { putString(KEY_ACTIVE_PROFILE_ID, profileId) }
        }
    }

    /**
     * Get all saved profiles
     */
    fun getAllProfiles(): List<SavedEQProfile> {
        return _profiles.value
    }

    /**
     * Get active profile
     */
    fun getActiveProfile(): SavedEQProfile? {
        return _activeProfile.value
    }

    /**
     * Import a custom EQ profile from ParametricEQ data
     */
    suspend fun importCustomProfile(
        name: String,
        parametricEQ: ParametricEQ
    ) = withContext(Dispatchers.IO) {
        // Generate unique ID for custom profile
        val id = "custom_${System.currentTimeMillis()}_${name.hashCode()}"

        val customProfile = SavedEQProfile(
            id = id,
            name = name,
            deviceModel = name,
            bands = parametricEQ.bands,  // Already ParametricEQBand
            preamp = parametricEQ.preamp,
            isActive = false,
            isCustom = true // Ensure this flag is set!
        )

        saveProfile(customProfile)
    }

    /**
     * Get profiles sorted by type: AutoEQ first, then custom profiles
     * Within each group, sort by timestamp (newest first)
     */
    fun getSortedProfiles(): List<SavedEQProfile> {
        // Only custom profiles are supported now
        return _profiles.value
            .filter { it.isCustom }
            .sortedByDescending { it.addedTimestamp }
    }

    private fun seedInitialPresets() {
        val frequencies = listOf(25.0, 40.0, 63.0, 100.0, 160.0, 250.0, 400.0, 630.0, 1000.0, 1600.0, 2500.0, 4000.0, 6300.0, 10000.0, 16000.0)
        
        fun createProfile(name: String, model: String, gains: List<Double>): SavedEQProfile {
            val bands = frequencies.zip(gains).map { (freq, gain) ->
                ParametricEQBand(
                    frequency = freq,
                    gain = gain,
                    q = 1.41,
                    filterType = FilterType.PK,
                    enabled = true
                )
            }
            return SavedEQProfile(
                id = "builtin_${name.lowercase().replace(" ", "_").replace("-", "_")}",
                name = name,
                deviceModel = model,
                bands = bands,
                preamp = -gains.maxOrNull()!!.coerceAtLeast(0.0), // Prevent clipping
                isCustom = true, // Show in presets list
                isActive = false
            )
        }

        val presets = listOf(
            createProfile("Flat Studio", "Reference 15-Band EQ", listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)),
            createProfile("Happy Solo", "Bright & Punchy EQ", listOf(2.0, 3.0, 4.0, 2.0, 0.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0)),
            createProfile("Deep Bass Pro", "Enhanced Sub & Mid-Bass EQ", listOf(6.0, 5.5, 4.0, 2.0, 0.0, -1.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0)),
            createProfile("Acoustic Clarity", "Crisp String & Vocal EQ", listOf(1.0, 2.0, 3.0, 2.0, 0.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0)),
            createProfile("Cinematic Power", "Immersive Theatre EQ", listOf(5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, 0.0, 2.0, 3.0, 4.0, 5.0, 6.0, 5.0, 4.0)),
            createProfile("Vocal Presence", "Podcast & Dialogue EQ", listOf(-1.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0)),
            createProfile("Electronic Drive", "EDM & Synth EQ", listOf(5.0, 4.0, 2.0, 0.0, -1.0, -2.0, -1.0, 0.0, 1.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0)),
            createProfile("Rock Stadium", "Live Concert EQ", listOf(3.0, 4.0, 3.0, 1.0, 0.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 4.0, 3.0, 2.0)),
            createProfile("Lo-Fi Chill", "Warm Vintage EQ", listOf(2.0, 3.0, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -2.0, -3.0, -4.0, -3.0, -2.0, -1.0, 0.0)),
            createProfile("Classical Hall", "Orchestral Sweep EQ", listOf(2.0, 3.0, 2.0, 1.0, 0.0, -1.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0)),
            createProfile("R&B Smooth", "Soulful Grooves EQ", listOf(4.0, 5.0, 3.0, 1.0, 0.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 3.0, 2.0, 1.0, 0.0))
        )

        _profiles.value = presets
        val profilesJson = json.encodeToString<List<SavedEQProfile>>(presets)
        prefs.edit { putString(KEY_PROFILES, profilesJson) }
    }
}
