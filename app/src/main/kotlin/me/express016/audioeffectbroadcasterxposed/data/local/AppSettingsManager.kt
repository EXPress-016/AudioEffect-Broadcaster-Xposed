package me.express016.audioeffectbroadcasterxposed.data.local

import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode

interface AppSettingsManager {
    fun getCheckedApps(): Set<String>
    fun saveCheckedApps(apps: Set<String>)
    fun getDelay(): Long
    fun saveDelay(value: Long)
    fun getExecutionMode(): ExecutionMode
    fun saveExecutionMode(mode: ExecutionMode)
}