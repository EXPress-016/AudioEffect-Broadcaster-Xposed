package me.express016.audioeffectbroadcasterxposed

import android.app.AndroidAppHelper
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

object Utils {
    fun broadcastAudioEffectAction(
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