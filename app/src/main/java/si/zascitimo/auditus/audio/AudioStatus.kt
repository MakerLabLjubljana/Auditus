package si.zascitimo.auditus.audio

data class AudioStatus(
    val missingBtDevice: Boolean,
    val missingPlaybackDevice: Boolean,
    val isStreamActive: Boolean,
    val inInternalSpeaker: Boolean
)