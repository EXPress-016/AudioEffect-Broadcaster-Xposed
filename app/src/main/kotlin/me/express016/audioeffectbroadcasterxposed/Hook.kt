package me.express016.audioeffectbroadcasterxposed

import android.app.AndroidAppHelper
import android.content.Intent
import android.media.AudioTrack
import android.media.audiofx.AudioEffect
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


@Keep
class AudioEffectBroadcasterHook : IXposedHookLoadPackage {

    companion object {
        const val TAG = "AEBX"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

        val hookedExoPlayerImpl = hookExoPlayerImpl(lpparam)

        if (!hookedExoPlayerImpl) hookAudioTrack(lpparam)

        Log.d(TAG, "Hooked ${lpparam.packageName}")
    }

    private fun hookAudioTrack(lpparam: XC_LoadPackage.LoadPackageParam) {

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

    private fun hookExoPlayerImpl(lpparam: XC_LoadPackage.LoadPackageParam): Boolean {
        try {
            val exoPlayerImpl = findExoPlayerImplClass(lpparam.classLoader)

            if (exoPlayerImpl == null) {
                Log.d(TAG, "No compatible ExoPlayer class found, trying android.media.AudioTrack.")
                return false
            }

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
            } catch (e: XposedHelpers.ClassNotFoundError) {
            }
        }

        return null
    }

    private fun broadcastAudioEffectAction(
        audioSessionId: Int, event: String
    ) {
        val targetContext = AndroidAppHelper.currentApplication()

        Log.d(
            TAG,
            "Broadcasting AudioEffect Event: $event with Session: $audioSessionId for ${targetContext.packageName}"
        )

        val intent = Intent(event).apply {
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, targetContext.packageName)
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        }

        targetContext.sendBroadcast(
            intent
        )
    }
}