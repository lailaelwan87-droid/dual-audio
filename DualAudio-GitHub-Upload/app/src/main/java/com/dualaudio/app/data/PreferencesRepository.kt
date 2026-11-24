package com.dualaudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dualaudio.app.audio.AppLanguage
import com.dualaudio.app.audio.AudioDelayBuffer
import com.dualaudio.app.audio.RoutingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dualAudioDataStore by preferencesDataStore(name = "dual_audio_prefs")

data class UserPreferences(
    val language: AppLanguage = AppLanguage.ARABIC,
    val darkTheme: Boolean = true,
    val routingMode: RoutingMode = RoutingMode.WIRED_BLUETOOTH,
    val bluetoothFirst: Boolean = false,
    val autoReapply: Boolean = true,
    val wiredDelayMs: Int = 200,
    val autoSyncLatency: Boolean = true,
    val wiredVolume: Float = 0.75f,
    val bluetoothVolume: Float = 0.75f
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val ROUTING_MODE = intPreferencesKey("routing_mode")
        val BLUETOOTH_FIRST = booleanPreferencesKey("bluetooth_first")
        val AUTO_REAPPLY = booleanPreferencesKey("auto_reapply")
        val WIRED_DELAY_MS = intPreferencesKey("wired_delay_ms")
        val AUTO_SYNC_LATENCY = booleanPreferencesKey("auto_sync_latency")
        val WIRED_VOLUME = floatPreferencesKey("wired_volume")
        val BLUETOOTH_VOLUME = floatPreferencesKey("bluetooth_volume")
    }

    val preferences: Flow<UserPreferences> = context.dualAudioDataStore.data.map { prefs ->
        UserPreferences(
            language = when (prefs[Keys.LANGUAGE]) {
                AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
                else -> AppLanguage.ARABIC
            },
            darkTheme = prefs[Keys.DARK_THEME] ?: true,
            routingMode = RoutingMode.fromCode(prefs[Keys.ROUTING_MODE] ?: RoutingMode.WIRED_BLUETOOTH.code),
            bluetoothFirst = prefs[Keys.BLUETOOTH_FIRST] ?: false,
            autoReapply = prefs[Keys.AUTO_REAPPLY] ?: true,
            wiredDelayMs = (prefs[Keys.WIRED_DELAY_MS] ?: 200).coerceIn(0, AudioDelayBuffer.MAX_DELAY_MS),
            autoSyncLatency = prefs[Keys.AUTO_SYNC_LATENCY] ?: true,
            wiredVolume = (prefs[Keys.WIRED_VOLUME] ?: 0.75f).coerceIn(0.0f, 1.0f),
            bluetoothVolume = (prefs[Keys.BLUETOOTH_VOLUME] ?: 0.75f).coerceIn(0.0f, 1.0f)
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dualAudioDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setDarkTheme(darkTheme: Boolean) {
        context.dualAudioDataStore.edit { it[Keys.DARK_THEME] = darkTheme }
    }

    suspend fun setRoutingMode(mode: RoutingMode) {
        context.dualAudioDataStore.edit { it[Keys.ROUTING_MODE] = mode.code }
    }

    suspend fun setBluetoothFirst(bluetoothFirst: Boolean) {
        context.dualAudioDataStore.edit { it[Keys.BLUETOOTH_FIRST] = bluetoothFirst }
    }

    suspend fun setAutoReapply(autoReapply: Boolean) {
        context.dualAudioDataStore.edit { it[Keys.AUTO_REAPPLY] = autoReapply }
    }

    suspend fun setWiredDelayMs(delayMs: Int) {
        context.dualAudioDataStore.edit {
            it[Keys.WIRED_DELAY_MS] = delayMs.coerceIn(0, AudioDelayBuffer.MAX_DELAY_MS)
        }
    }

    suspend fun setAutoSyncLatency(enabled: Boolean) {
        context.dualAudioDataStore.edit { it[Keys.AUTO_SYNC_LATENCY] = enabled }
    }

    suspend fun setWiredVolume(volume: Float) {
        context.dualAudioDataStore.edit {
            it[Keys.WIRED_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }

    suspend fun setBluetoothVolume(volume: Float) {
        context.dualAudioDataStore.edit {
            it[Keys.BLUETOOTH_VOLUME] = volume.coerceIn(0.0f, 1.0f)
        }
    }
}
