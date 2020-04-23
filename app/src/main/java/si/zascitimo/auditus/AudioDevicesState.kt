package si.zascitimo.auditus

import android.media.AudioDeviceInfo
import com.google.sample.audio_device.AudioDeviceInfoConverter

class AudioDevicesState(
    private val currentRecording: AudioDeviceInfo?,
    private val currentPlayback: AudioDeviceInfo?,
    private val selectedBt: AudioDeviceInfo?,
    private val selectedWired: AudioDeviceInfo?,
    private val selectedSpeaker: AudioDeviceInfo?,
    private val customRecording: AudioDeviceInfo?,
    private val customPlayback: AudioDeviceInfo?
) {
    private fun deviceInfoToString(device: AudioDeviceInfo?): String {
        return if (device == null) {
            "null"
        } else {
            device.productName.toString() + " " + AudioDeviceInfoConverter.typeToString(device.type)
        }
    }

    override fun toString(): String {
        return "Active recording device: ${deviceInfoToString(currentRecording)} \n" +
                "Active playback device: ${deviceInfoToString(currentPlayback)} \n" +
                "Auto selected bt device: ${deviceInfoToString(selectedBt)} \n" +
                "Auto selected wired device: ${deviceInfoToString(selectedWired)} \n" +
                "Auto selected speaker device: ${deviceInfoToString(selectedSpeaker)} \n" +
                "Custom recording device: ${deviceInfoToString(customRecording)} \n" +
                "Custom playback device: ${deviceInfoToString(customPlayback)} \n"
    }
}