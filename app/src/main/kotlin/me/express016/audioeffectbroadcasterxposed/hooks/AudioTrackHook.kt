package me.express016.audioeffectbroadcasterxposed.hooks

import android.media.AudioTrack
import android.media.audiofx.AudioEffect
import android.util.Log
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.express016.audioeffectbroadcasterxposed.TAG
import me.express016.audioeffectbroadcasterxposed.Utils.broadcastAudioEffectAction

fun hookAudioTrack(lpparam: XC_LoadPackage.LoadPackageParam) {

    try {
        val audioTrack =
            XposedHelpers.findClass("android.media.AudioTrack", lpparam.classLoader)

        Log.d(TAG, "Hooking ${audioTrack.packageName}.${audioTrack.name}")

        XposedHelpers.findAndHookMethod(
            audioTrack, "play", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    val track = param?.thisObject as AudioTrack?

                    track?.let {
                        if (track.playState != AudioTrack.PLAYSTATE_PLAYING) return@let param.result

                        val audioSessionId = track.audioSessionId

                        broadcastAudioEffectAction(
                            audioSessionId, AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION
                        )

                    }

                }
            })

        arrayOf("pause", "stop", "release", "flush").forEach {
            XposedHelpers.findAndHookMethod(
                audioTrack, it, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam?) {
                        val track = param?.thisObject as AudioTrack?

                        track?.let {
                            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) return@let param.result

                            val audioSessionId = track.audioSessionId

                            broadcastAudioEffectAction(
                                audioSessionId,
                                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
                            )

                        }
                    }
                })
        }
    } catch (e: Throwable) {
        Log.e(TAG, "hookAudioTrack Error", e)
    }
}