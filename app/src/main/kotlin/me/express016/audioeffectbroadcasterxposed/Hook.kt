package me.express016.audioeffectbroadcasterxposed

import android.util.Log
import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.express016.audioeffectbroadcasterxposed.hooks.hookAudioTrack
import me.express016.audioeffectbroadcasterxposed.hooks.hookExoPlayerImpl

val forceAudioTrackPackages = arrayOf(
    // Has ExoPlayer but doesn't use it
    "com.apple.android.music"
)

@Keep
class AudioEffectBroadcasterHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (forceAudioTrackPackages.contains(lpparam.packageName)) {
            Log.d(
                TAG,
                "${lpparam.packageName} is in force AudioTrack list, trying android.media.AudioTrack"
            )
            hookAudioTrack(lpparam)
        } else {
            val hookedExoPlayerImpl = hookExoPlayerImpl(lpparam)

            if (!hookedExoPlayerImpl) {
                Log.d(TAG, "No compatible ExoPlayer class found, trying android.media.AudioTrack.")
                hookAudioTrack(lpparam)
            }
        }

        Log.d(TAG, "Hooked ${lpparam.packageName}")
    }
}