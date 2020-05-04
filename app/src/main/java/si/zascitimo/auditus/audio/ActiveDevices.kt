package si.zascitimo.auditus.audio

import android.media.AudioDeviceInfo

data class ActiveDevices(
    val wiredSpeaker: AudioDeviceInfo? = null,
    val internalSpeaker: AudioDeviceInfo? = null,
    val btDevice: AudioDeviceInfo? = null,
    val customRecording: AudioDeviceInfo? = null,
    val customPlayback: AudioDeviceInfo? = null,
    val skipBt: Boolean = false
) {
    fun hasRecordingDevice() = btDevice != null || skipBt

    fun hasPlaybackDevice() = wiredSpeaker != null || internalSpeaker != null
}