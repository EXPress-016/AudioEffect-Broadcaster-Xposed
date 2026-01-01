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
**AudioEffect-Broadcaster-Xposed** is an Xposed module that solves this problem by hooking into the `ExoPlayerImpl` class, a core component of the widely used `androidx.media3` media playback library.

It specifically monitors calls to the `setPlayWhenReady` method. When an app calls this method to start playback (`true`), the module broadcasts the `ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION` intent. When playback is paused (`false`), it broadcasts the `ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION` intent.

By doing this, it effectively makes many modern apps compatible with session-based audio effect controllers like Wavelet and Viper4Android.