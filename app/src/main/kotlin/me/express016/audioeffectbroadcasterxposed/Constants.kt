package me.express016.audioeffectbroadcasterxposed

const val TAG = "AEBX"

val DEFAULT_FORCE_AUDIOTRACK_PACKAGES = setOf(
    // Has ExoPlayer but doesn't use it
    "com.apple.android.music"
)

const val PREFS = "prefs"
const val PREFS_FORCED_AUDIOTRACK_PACKAGES = "forced_audiotrack_packages"
const val PREFS_DELAYS = "delays"
const val PREFS_EXECUTION_MODE = "execution_mode"