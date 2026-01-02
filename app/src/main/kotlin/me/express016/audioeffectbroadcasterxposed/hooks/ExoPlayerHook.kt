package me.express016.audioeffectbroadcasterxposed.hooks

import android.media.audiofx.AudioEffect
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.express016.audioeffectbroadcasterxposed.TAG
import me.express016.audioeffectbroadcasterxposed.Utils.broadcastAudioEffectAction

fun hookExoPlayerImpl(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
    try {
        val exoPlayerImpl = findExoPlayerImplClass(lpparam.classLoader) ?: return false

        Log.d(TAG, "Hooking ${exoPlayerImpl.packageName}.${exoPlayerImpl.name}")

        XposedHelpers.findAndHookMethod(
            exoPlayerImpl,
            "setPlayWhenReady",
            Boolean::class.javaPrimitiveType,
            object : XC_MethodHook() {
                @OptIn(UnstableApi::class)
                override fun afterHookedMethod(param: MethodHookParam?) {
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
                        ) as Int? ?: return

                        broadcastAudioEffectAction(audioSessionId, action)
                    }
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