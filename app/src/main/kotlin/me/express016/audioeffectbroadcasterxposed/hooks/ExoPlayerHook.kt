package me.express016.audioeffectbroadcasterxposed.hooks

import android.media.audiofx.AudioEffect
import android.os.Handler
import android.os.Looper
import android.util.Log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.express016.audioeffectbroadcasterxposed.TAG
import me.express016.audioeffectbroadcasterxposed.Utils.broadcastAudioEffectAction
import me.express016.audioeffectbroadcasterxposed.model.ExecutionMode
import me.express016.audioeffectbroadcasterxposed.toHook


fun hookExoPlayerImpl(
    lpparam: XC_LoadPackage.LoadPackageParam,
    delay: Long = 0,
    executionMode: ExecutionMode = ExecutionMode.POST_HOOK
): Boolean {
    try {
        val exoPlayerImpl = findExoPlayerImplClass(lpparam.classLoader) ?: return false

        Log.d(TAG, "Hooking ${exoPlayerImpl.name}")

        XposedHelpers.findAndHookMethod(
            exoPlayerImpl, "setPlayWhenReady", Boolean::class.javaPrimitiveType,
            executionMode.toHook { param ->
                val playerInstance = param?.thisObject

                playerInstance?.let {
                    val playWhenReady = param.args[0] as Boolean

                    val action = if (playWhenReady) {
                        AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                    } else {
                        AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
                    }

                    val audioSessionId = XposedHelpers.callMethod(
                        playerInstance, "getAudioSessionId"
                    ) as Int? ?: return@let

                    Handler(Looper.getMainLooper()).postDelayed({
                        broadcastAudioEffectAction(audioSessionId, action)
                    }, delay)
                }
            })

        return true
    } catch (e: Throwable) {
        Log.e(TAG, "hookExoPlayerImpl Error", e)
        return false
    }
}

private fun findExoPlayerImplClass(classLoader: ClassLoader): Class<*>? {
    val classNames = listOf(
        "androidx.media3.exoplayer.ExoPlayerImpl",
        "com.google.android.exoplayer2.ExoPlayerImpl",
    )

    for (className in classNames) {
        try {
            return XposedHelpers.findClass(className, classLoader)
        } catch (_: XposedHelpers.ClassNotFoundError) {
        }
    }

    return null
}