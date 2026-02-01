package me.express016.audioeffectbroadcasterxposed.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.express016.audioeffectbroadcasterxposed.data.local.AppSettingsManager
import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode
import me.express016.audioeffectbroadcasterxposed.model.SheetType


class HomeViewModel(private val settings: AppSettingsManager) : ViewModel() {

    private val checkedAppsState = MutableStateFlow<Set<String>>(emptySet())
    val checkedApps: StateFlow<Set<String>> = checkedAppsState.asStateFlow()


    private val delayState = MutableStateFlow(0L)
    val delay: StateFlow<Long> = delayState.asStateFlow()


    private val executionModeState = MutableStateFlow(ExecutionMode.POST_HOOK)
    val executionMode: StateFlow<ExecutionMode> = executionModeState.asStateFlow()


    var currentSheet by mutableStateOf(SheetType.NONE)
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            checkedAppsState.value = settings.getCheckedApps()

            delayState.value = settings.getDelay()

            executionModeState.value = settings.getExecutionMode()

        }
    }


    fun toggleApp(packageName: String) {
        checkedAppsState.update { currentSet ->
            val newSet = currentSet.toMutableSet()
            if (!newSet.add(packageName)) {
                newSet.remove(packageName)
            }

            viewModelScope.launch(Dispatchers.IO) {
                settings.saveCheckedApps(newSet)
            }

            newSet
        }
    }


    fun saveDelay(value: Long) {
        delayState.value = value
        viewModelScope.launch(Dispatchers.IO) {
            settings.saveDelay(value)
        }
    }


    fun saveExecutionMode(value: ExecutionMode) {
        executionModeState.value = value

        viewModelScope.launch(Dispatchers.IO) {
            settings.saveExecutionMode(value)
        }
    }


    fun onMenuItemClicked(itemId: String) {
        currentSheet = when (itemId) {
            "forced_audiotrack_apps" -> {
                SheetType.FORCED_AUDIOTRACK_APPS
            }

            "misc" -> {
                SheetType.MISCELLANEOUS
            }

            else -> {
                SheetType.NONE
            }
        }
    }

    fun dismissSheet() {
        currentSheet = SheetType.NONE
    }

}