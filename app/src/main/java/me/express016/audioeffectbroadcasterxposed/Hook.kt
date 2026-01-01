package me.express016.audioeffectbroadcasterxposed

import android.content.Context
import android.content.Intent
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
    val TAG = "AEBX"

    // var targetContext: Context? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

//        try {
//            var cls = XposedHelpers.findClass(
//                "androidx.media3.exoplayer.ExoPlayerImpl",
//                lpparam.classLoader
//            )
//
//            Log.i(
//                TAG,
//                cls.methods.filter { it.name.lowercase().contains("addListener".lowercase()) }
//                    .joinToString("\n").toString()
//            )
//        } catch (e: Throwable) {
//            Log.e(TAG, "Error", e)
//        }


        // hookOnCreate(lpparam)
        hookExoPlayerImpl(lpparam)

        Log.d(TAG, "Hooked ${lpparam.packageName}")
    }

//    private fun hookOnCreate(lpparam: XC_LoadPackage.LoadPackageParam) {
//        XposedHelpers.findAndHookMethod(
//            "android.app.Application", lpparam.classLoader, "onCreate", object : XC_MethodHook() {
//                @Throws(Throwable::class)
//                override fun afterHookedMethod(param: MethodHookParam) {
//                    targetContext = param.thisObject as Context?
//                }
//            })
//    }

    private fun hookExoPlayerImpl(lpparam: XC_LoadPackage.LoadPackageParam) {
        try {
            val exoPlayerImpl = XposedHelpers.findClass(
                "androidx.media3.exoplayer.ExoPlayerImpl", lpparam.classLoader
            )

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

                            broadcastAudioEffectAction(playerInstance, action)
                        }
                    }
                })

        } catch (e: Throwable) {
            Log.e(TAG, "Error", e)
        }
    }

    private fun broadcastAudioEffectAction(playerInstance: Any, event: String) {
        val audioSessionId =
            XposedHelpers.callMethod(playerInstance, "getAudioSessionId") as Int? ?: return

        if (audioSessionId == -1) return

        val targetContext =
            XposedHelpers.getObjectField(playerInstance, "applicationContext") as? Context ?: return

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