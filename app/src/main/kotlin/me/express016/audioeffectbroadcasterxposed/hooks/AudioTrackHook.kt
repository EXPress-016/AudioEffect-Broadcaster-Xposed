package me.express016.audioeffectbroadcasterxposed.hooks

import android.media.AudioTrack
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

fun hookAudioTrack(
    lpparam: XC_LoadPackage.LoadPackageParam,
    delay: Long = 0,
    executionMode: ExecutionMode = ExecutionMode.POST_HOOK
) {

    try {
        val audioTrack = XposedHelpers.findClass("android.media.AudioTrack", lpparam.classLoader)

        Log.d(TAG, "Hooking ${audioTrack.name}")

        XposedHelpers.findAndHookMethod(
            audioTrack, "play", executionMode.toHook { param ->
                val track = param?.thisObject as? AudioTrack
                track?.takeIf {
                    it.playState == AudioTrack.PLAYSTATE_PLAYING || (executionMode == ExecutionMode.PRE_HOOK && it.playState != AudioTrack.PLAYSTATE_PLAYING)
                }?.let {
                    Handler(Looper.getMainLooper()).postDelayed({

                        broadcastAudioEffectAction(
                            it.audioSessionId,
                            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                        )
                    }, delay)
                }
            })

        arrayOf("pause", "stop", "release", "flush").forEach { methodName ->
            XposedHelpers.findAndHookMethod(
                audioTrack, methodName, executionMode.toHook { param ->

                    val track = param?.thisObject as AudioTrack?

                    track?.takeIf {
                        it.playState != AudioTrack.PLAYSTATE_PLAYING || (executionMode == ExecutionMode.PRE_HOOK && it.playState == AudioTrack.PLAYSTATE_PLAYING)
                    }?.let {
                        val audioSessionId = track.audioSessionId

                        Handler(Looper.getMainLooper()).postDelayed({
                            broadcastAudioEffectAction(
                                audioSessionId,
                                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
                            )
                        }, delay)


                    }


                    return@toHook param?.result
                })
        }
    } catch (e: Throwable) {
        Log.e(TAG, "hookAudioTrack Error", e)
    }
}