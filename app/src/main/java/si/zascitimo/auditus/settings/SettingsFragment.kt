package si.zascitimo.auditus.settings

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.sample.audio_device.AudioDeviceListEntry
import si.zascitimo.auditus.App
import si.zascitimo.auditus.R
import si.zascitimo.auditus.prefs

class SettingsFragment : PreferenceFragmentCompat() {
    private var audioManager: AudioManager? = null
    private val audioRouter by lazy { (requireActivity().application as App).audioRouter }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun setDevices(devices: Array<out AudioDeviceInfo>) {
        val recList = AudioDeviceListEntry.createListFrom(devices, AudioManager.GET_DEVICES_INPUTS)
        val playList = AudioDeviceListEntry.createListFrom(devices, AudioManager.GET_DEVICES_OUTPUTS)


        val recPreference: ListPreference? = findPreference("recording")
        recPreference?.apply {
            entries = arrayOf(getString(R.string.auto_select)) + recList.map { it.name }.toTypedArray()
            entryValues = arrayOf("0") + recList.map { it.id.toString() }.toTypedArray()
            value = when (findIndexOfValue(audioRouter.customRecordingDevice.toString())) {
                -1 -> "0"
                else -> audioRouter.customRecordingDevice.toString()
            }
            summary = entry
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                audioRouter.customRecordingDevice = (newValue as String).toInt()
                summary = entries[findIndexOfValue(newValue)]
                true
            }
        }

        val playPreference: ListPreference? = findPreference("playback")
        playPreference?.apply {
            entries = arrayOf(getString(R.string.auto_select)) + playList.map { it.name }.toTypedArray()
            entryValues = arrayOf("0") + playList.map { it.id.toString() }.toTypedArray()
            value = when (findIndexOfValue(audioRouter.customPlaybackDevice.toString())) {
                -1 -> "0"
                else -> audioRouter.customPlaybackDevice.toString()
            }
            summary = entry
            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                audioRouter.customPlaybackDevice = (newValue as String).toInt()
                summary = entries[findIndexOfValue(newValue)]
                true
            }
        }
    }

    val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            if (addedDevices != null) {
                setDevices(addedDevices)
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            if (removedDevices != null) {
                setDevices(removedDevices)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audioManager?.registerAudioDeviceCallback(callback, null)
    }

    override fun onStop() {
        audioManager?.unregisterAudioDeviceCallback(callback)
        super.onStop()
    }
}