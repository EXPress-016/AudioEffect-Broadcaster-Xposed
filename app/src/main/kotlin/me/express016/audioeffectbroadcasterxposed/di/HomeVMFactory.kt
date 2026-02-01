package me.express016.audioeffectbroadcasterxposed.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import me.express016.audioeffectbroadcasterxposed.data.local.AppSettingsManager
import me.express016.audioeffectbroadcasterxposed.ui.screens.home.HomeViewModel

@Suppress("UNCHECKED_CAST")
class HomeVMFactory(private val settings: AppSettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(settings) as T
    }
}