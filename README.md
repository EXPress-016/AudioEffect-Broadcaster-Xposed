# AudioEffect-Broadcaster-Xposed
An Xposed module designed to broadcast audio session creation and destruction events system-wide. This allows applications like Wavelet and Viper4Android to detect audio streams from any app and apply their audio effects, even from apps that don't normally support it.

## Requirements
*   A compatible Xposed Framework installed (e.g. LSPosed).

## Installation
1.  Download the latest release of the AudioEffect-Broadcaster-Xposed module from the [releases](../../releases) page.
2.  Install the APK on your device.
3.  Open your Xposed manager app (e.g. LSPosed).
4.  Enable the **AudioEffect-Broadcaster-Xposed** module and ensure it is scoped to the apps you want to affect.

## Problem
Many popular audio effect applications, such as Wavelet and Viper4Android, rely on Android's standard AudioEffect broadcasts (`ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION` and `ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION`) to detect when an audio session starts or ends. However, not all applications send these broadcasts, which means that these audio effect apps won't detect the audio and therefore won't apply any effects.

## Solution
**AudioEffect-Broadcaster-Xposed** forces applications to announce their audio sessions. It does this by hooking into the Android media framework with two-layer approach:

First, it targets `ExoPlayer` (`androidx.media3` and `com.google.android.exoplayer2`), the most common media player library. By monitoring the `setPlayWhenReady` method, it can tell when audio starts and stops, and broadcasts the standard `ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION` and `ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION` intents accordingly.

As a fallback for apps that don't use ExoPlayer, the module hooks the fundamental `android.media.AudioTrack` class. By watching for `play` and `pause`/`stop`/`release`/`flush` events, it ensures nearly all audio playback is detected and broadcasted correctly.

This makes previously unsupported apps visible to audio effect engines like Wavelet and Viper4Android.
