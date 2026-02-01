package me.express016.audioeffectbroadcasterxposed

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import me.express016.audioeffectbroadcasterxposed.data.local.AppSettingsManagerImpl
import me.express016.audioeffectbroadcasterxposed.di.HomeVMFactory
import me.express016.audioeffectbroadcasterxposed.ui.screens.home.HomeScreen
import me.express016.audioeffectbroadcasterxposed.ui.screens.home.HomeViewModel
import me.express016.audioeffectbroadcasterxposed.ui.theme.AudioEffectBroadcasterTheme

class MainActivity : ComponentActivity() {

    private val prefs by lazy {
        getSharedPreferences(PREFS, MODE_PRIVATE)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AudioEffectBroadcasterTheme {
                Scaffold {
                    val viewModel by viewModels<HomeViewModel> {
                        HomeVMFactory(AppSettingsManagerImpl(prefs))
                    }
                    HomeScreen(viewModel, ::isModuleActive)
                }
            }
        }
    }

    @Keep
    fun isModuleActive() = false

}
