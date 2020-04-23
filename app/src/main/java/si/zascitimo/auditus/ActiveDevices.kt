package si.zascitimo.auditus

import android.media.AudioDeviceInfo

data class ActiveDevices(
    val wiredSpeaker: AudioDeviceInfo? = null,
    val internalSpeaker: AudioDeviceInfo? = null,
    val btDevice: AudioDeviceInfo? = null,
    val customRecording: AudioDeviceInfo? = null,
    val customPlayback: AudioDeviceInfo? = null
) {
    fun hasRecordingDevice() = btDevice != null

    fun hasPlaybackDevice() = wiredSpeaker != null || internalSpeaker != null
}