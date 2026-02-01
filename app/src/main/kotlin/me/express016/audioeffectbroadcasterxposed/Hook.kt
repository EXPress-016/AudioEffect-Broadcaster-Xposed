package me.express016.audioeffectbroadcasterxposed

import android.util.Log
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.express016.audioeffectbroadcasterxposed.hooks.hookAudioTrack
import me.express016.audioeffectbroadcasterxposed.hooks.hookExoPlayerImpl
import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode

class Hook @Keep constructor() : IXposedHookLoadPackage {
    val prefs: XSharedPreferences by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, PREFS).apply {
            makeWorldReadable()
            reload()
        }
    }

    val forceAudioTrackPackages =
        prefs.getStringSet(PREFS_FORCED_AUDIOTRACK_PACKAGES, DEFAULT_FORCE_AUDIOTRACK_PACKAGES)
            ?: DEFAULT_FORCE_AUDIOTRACK_PACKAGES
    val delay = prefs.getLong(PREFS_DELAYS, 0)
    val executionMode = ExecutionMode.entries.getOrElse(
        prefs.getInt(PREFS_EXECUTION_MODE, ExecutionMode.POST_HOOK.ordinal)
    ) { ExecutionMode.POST_HOOK }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

        when (lpparam.packageName) {
            BuildConfig.APPLICATION_ID -> {
                val appClazz =
                    lpparam.classLoader.loadClass("${BuildConfig.APPLICATION_ID}.MainActivity")

                XposedHelpers.findAndHookMethod(
                    appClazz, "isModuleActive", XC_MethodReplacement.returnConstant(true)
                )

                Log.d(TAG, "Hooked $appClazz")
            }

            else -> {
                if (forceAudioTrackPackages.contains(lpparam.packageName)) {
                    Log.d(
                        TAG,
                        "${lpparam.packageName} is in force AudioTrack list, trying android.media.AudioTrack"
                    )
                    hookAudioTrack(lpparam, delay, executionMode)
                } else {
                    val hookedExoPlayerImpl = hookExoPlayerImpl(lpparam, delay, executionMode)

                    if (!hookedExoPlayerImpl) {
                        Log.d(
                            TAG,
                            "No compatible ExoPlayer class found, trying android.media.AudioTrack."
                        )
                        hookAudioTrack(lpparam, delay, executionMode)
                    }
                }

                Log.d(TAG, "Hooked ${lpparam.packageName}")
            }
        }

    }
}

fun ExecutionMode.toHook(block: (XC_MethodHook.MethodHookParam?) -> Any?): XC_MethodHook {
    return when (this@toHook) {
        ExecutionMode.PRE_HOOK -> {
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    block(param)
                }
            }
        }

        ExecutionMode.POST_HOOK -> {
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    block(param)
                }
            }
        }
    }

}