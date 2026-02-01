package me.express016.audioeffectbroadcasterxposed.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import me.express016.audioeffectbroadcasterxposed.DEFAULT_FORCE_AUDIOTRACK_PACKAGES
import me.express016.audioeffectbroadcasterxposed.PREFS_DELAYS
import me.express016.audioeffectbroadcasterxposed.PREFS_EXECUTION_MODE
import me.express016.audioeffectbroadcasterxposed.PREFS_FORCED_AUDIOTRACK_PACKAGES
import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode

class AppSettingsManagerImpl(private val prefs: SharedPreferences) : AppSettingsManager {
    override fun getCheckedApps(): Set<String> {
        return prefs.getStringSet(
            PREFS_FORCED_AUDIOTRACK_PACKAGES, DEFAULT_FORCE_AUDIOTRACK_PACKAGES
        ) ?: DEFAULT_FORCE_AUDIOTRACK_PACKAGES
    }

    override fun saveCheckedApps(apps: Set<String>) {
        prefs.edit { putStringSet(PREFS_FORCED_AUDIOTRACK_PACKAGES, apps) }
    }

    override fun getDelay(): Long {
        return prefs.getLong(PREFS_DELAYS, 0)
    }

    override fun saveDelay(value: Long) {
        prefs.edit { putLong(PREFS_DELAYS, value) }
    }

    override fun getExecutionMode(): ExecutionMode {
        val savedExecutionMode = prefs.getInt(PREFS_EXECUTION_MODE, ExecutionMode.POST_HOOK.ordinal)
        return ExecutionMode.entries.getOrElse(savedExecutionMode) { ExecutionMode.POST_HOOK }
    }

    override fun saveExecutionMode(mode: ExecutionMode) {
        prefs.edit { putInt(PREFS_EXECUTION_MODE, mode.ordinal) }
    }
}