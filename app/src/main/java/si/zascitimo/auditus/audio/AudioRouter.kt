package com.example.auditus

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.GET_DEVICES_ALL
import android.os.Build
import android.os.SystemClock
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import si.zascitimo.auditus.*
import si.zascitimo.auditus.audio.ActiveDevices
import si.zascitimo.auditus.audio.AudioDevicesState
import timber.log.Timber

class AudioRouter(
    private val context: Context,
    private val DBG: Boolean = BuildConfig.DEBUG
) {

    private val audioManager: AudioManager? =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?)?.adapter
    private var bluetoothHeadset: BluetoothHeadset? = null
    private var bluetoothHeadsetState = BluetoothProfile.STATE_DISCONNECTED
    private var bluetoothHeadsetAudioState = BluetoothHeadset.STATE_AUDIO_DISCONNECTED
    private var bluetoothConnectionPending = false
    private var bluetoothConnectionRequestTime: Long = 0

    private var audioRequest: AudioFocusRequest? = null

    private var receiverRegistered: Boolean = false
    private var isInited: Boolean = false

    private var mutableActiveDevice = MutableLiveData<ActiveDevices>()
    val activeDevice: LiveData<ActiveDevices>
        get() = mutableActiveDevice

    private var currentRecordingDevice: AudioDeviceInfo? = null
    private var currentPlaybackDevice: AudioDeviceInfo? = null

    var customRecordingDevice: Int? = prefs.recordDevice
        set(value) {
            field = value
            updateState()
        }
    var customPlaybackDevice: Int? = prefs.playbackDevice
        set(value) {
            field = value
            updateState()
        }

    private var isStarted = false

    private var mutableIsStreamActive = MutableLiveData<Boolean>()
    val isStreamActive: LiveData<Boolean>
        get() = mutableIsStreamActive

    private var mutableAudioDeviceState = MutableLiveData<AudioDevicesState>()
    val audioDeviceState: LiveData<AudioDevicesState>
        get() = mutableAudioDeviceState

    fun init(): Boolean {
        if (isInited) {
            return false
        }

        mutableIsStreamActive.value = false

        createAudioEngine()

        isInited = true

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)

        if (bluetoothAdapter != null) {
            bluetoothAdapter.getProfileProxy(
                context,
                bluetoothProfileServiceListener,
                BluetoothProfile.HEADSET
            )

            intentFilter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            intentFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)
        }

        context.registerReceiver(audioBroadcastReceiver, intentFilter)
        receiverRegistered = true

        audioManager?.let {
            val sampleRateStr =
                it.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
            val defaultSampleRate = sampleRateStr.toInt()
            val framesPerBurstStr =
                it.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
            val defaultFramesPerBurst = framesPerBurstStr.toInt()

            setDefaultStreamValues(
                defaultSampleRate,
                defaultFramesPerBurst
            )
        }

        return true
    }

    fun start(): Boolean {
        if (isStarted) {
            return false
        }

        isStarted = startNativeStream()

        requestAudioFocus(AudioManager.STREAM_VOICE_CALL)

        return true
    }

    fun stop() {
        isStarted = false

        stopNativeStream()

        currentRecordingDevice = null
        currentPlaybackDevice = null

        abandonAudioFocus()
    }

    fun destroy() {
        if (!isInited) {
            return
        }

        isInited = false

        stop()

        if (receiverRegistered) {
            context.unregisterReceiver(audioBroadcastReceiver)
            receiverRegistered = false
        }

        if (bluetoothAdapter != null && bluetoothHeadset != null) {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
        }

        disconnectBluetooth()
    }

    private fun startNativeStream(): Boolean {
        val activeDevice = mutableActiveDevice.value

        val recordingDevice = when {
            activeDevice?.customRecording != null -> activeDevice.customRecording
            activeDevice?.btDevice != null -> activeDevice.btDevice
            else -> null
        }

        val playbackDevice = when {
            activeDevice?.customPlayback != null -> activeDevice.customPlayback
            activeDevice?.wiredSpeaker != null -> activeDevice.wiredSpeaker
            activeDevice?.internalSpeaker != null -> activeDevice.internalSpeaker
            else -> null
        }

        mutableAudioDeviceState.value =
            AudioDevicesState(
                recordingDevice,
                playbackDevice,
                activeDevice?.btDevice,
                activeDevice?.wiredSpeaker,
                activeDevice?.internalSpeaker,
                activeDevice?.customRecording,
                activeDevice?.customPlayback
            )

        if (recordingDevice == null) {
            Timber.w("Missing recordingDevice")
            Toast.makeText(context, "Missing recordingDevice", Toast.LENGTH_LONG).show()
            stopNativeStream()
            return false
        }

        if (playbackDevice == null) {
            Timber.w("Missing playbackDevice")
            Toast.makeText(context, "Missing playbackDevice", Toast.LENGTH_LONG).show()
            stopNativeStream()
            return false
        }

        if (recordingDevice != currentRecordingDevice || playbackDevice != currentPlaybackDevice) {
            currentRecordingDevice = recordingDevice
            currentPlaybackDevice = playbackDevice

            stopNativeStream()

            mutableIsStreamActive.value = true

            startStream(recordingDevice.id, playbackDevice.id)
        }
        return true
    }

    private fun stopNativeStream() {
        mutableIsStreamActive.value = false

        stopStream()
    }

    private fun requestAudioFocus(streamType: Int) {
        audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            audioRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(afChangeListener)
                .setAcceptsDelayedFocusGain(false)
                .build()

            audioManager.requestAudioFocus(audioRequest!!)
        } else {
            audioManager.requestAudioFocus(
                afChangeListener,
                streamType,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun abandonAudioFocus() {
        audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            audioManager.abandonAudioFocus(afChangeListener)
        }
    }

    private fun updateState() {
        val devices = audioManager?.getDevices(GET_DEVICES_ALL) ?: return
        val wiredSpeaker = devices.find {
            it.isSink && (it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
        }
        val internalSpeaker = devices.find {
            it.isSink && it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        }
        val btDevice = devices.find {
            it.isSource && it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
        val customRecording = devices.find {
            it.id == customRecordingDevice
        }
        val customPlayback = devices.find {
            it.id == customPlaybackDevice
        }
        mutableActiveDevice.value = ActiveDevices(
            wiredSpeaker = wiredSpeaker,
            internalSpeaker = internalSpeaker,
            btDevice = btDevice,
            customRecording = customRecording,
            customPlayback = customPlayback
        )

        if (isStarted && isInited) {
            startNativeStream()
        }
    }

    private fun isHeadsetPlugged(): Boolean = audioManager?.isWiredHeadsetOn ?: false

    private fun isSpeakerOn(): Boolean = audioManager?.isSpeakerphoneOn ?: false

    private fun setSpeaker(on: Boolean) {
        Timber.d("AudioManager mode: %d", audioManager?.mode ?: -1)
        try {
            audioManager?.isSpeakerphoneOn = on
        } catch (e: java.lang.Exception) {
            Timber.w(e, "Error setting AudioManager mode")
        }
    }

    private fun isBluetoothAvailable(): Boolean = when {
        bluetoothHeadsetState == BluetoothHeadset.STATE_CONNECTED -> true
        bluetoothHeadset?.connectedDevices?.isNotEmpty() ?: false -> true
        else -> false
    }

    private fun isBluetoothActive(): Boolean {
        val device = bluetoothHeadset?.connectedDevices?.firstOrNull()
        return if (device != null) {
            bluetoothHeadset?.isAudioConnected(device) ?: false
        } else {
            false
        }
    }

    private fun tryConnectBluetooth() {
        if (bluetoothHeadsetState == BluetoothHeadset.STATE_CONNECTED &&
            bluetoothHeadsetAudioState == BluetoothHeadset.STATE_AUDIO_DISCONNECTED
        ) {
            connectBluetooth()
        }
    }

    private fun tryDisconnectBluetooth() {
        if (bluetoothHeadsetState == BluetoothHeadset.STATE_DISCONNECTED &&
            bluetoothHeadsetAudioState == BluetoothHeadset.STATE_AUDIO_CONNECTED
        ) {
            disconnectBluetooth()
        }
    }

    private fun connectBluetooth() {
        if (isBluetoothAudioConnectedOrPending()) {
            return
        }
        Timber.d("Connecting bluetooth")

        if (bluetoothHeadset != null && audioManager?.isBluetoothScoAvailableOffCall == true) {
            Timber.d("Starting BluetoothSco")
            audioManager.isBluetoothScoOn = true
            audioManager.startBluetoothSco()
        }

        // Watch out: The bluetooth connection doesn't happen instantly;
        // the connectAudio() call returns instantly but does its real
        // work in another thread.  The mBluetoothConnectionPending flag
        // is just a little trickery to ensure that the onscreen UI updates
        // instantly. (See isBluetoothAudioConnectedOrPending() above.)
        bluetoothConnectionPending = true
        bluetoothConnectionRequestTime = SystemClock.elapsedRealtime()
    }

    private fun isBluetoothAudioConnectedOrPending(): Boolean {
        if (isBluetoothActive()) {
            return true
        }

        // If we issued a connectAudio() call "recently enough", even
        // if BT isn't actually connected yet, let's still pretend BT is
        // on.  This makes the onscreen indication more responsive.
        if (bluetoothConnectionPending) {
            val timeSinceRequest = SystemClock.elapsedRealtime() - bluetoothConnectionRequestTime
            return if (timeSinceRequest < 5000 /* 5 seconds */) {
                if (DBG) {
                    Timber.d("isBluetoothAudioConnectedOrPending: ==> TRUE (requested $timeSinceRequest msec ago)")
                }
                true
            } else {
                if (DBG) {
                    Timber.d("isBluetoothAudioConnectedOrPending: ==> FALSE (request too old: $timeSinceRequest msec ago)")
                }
                bluetoothConnectionPending = false
                false
            }
        }

        if (DBG) {
            Timber.d("isBluetoothAudioConnectedOrPending: ==> FALSE")
        }
        return false
    }

    private fun disconnectBluetooth() {
        Timber.d("Disconnecting bluetooth")

        if (bluetoothHeadset != null && audioManager != null) {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
        }

        bluetoothConnectionPending = false
    }

    private val afChangeListener =
        AudioManager.OnAudioFocusChangeListener { focusChange ->
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                if (DBG) {
                    Timber.d("AUDIOFOCUS_LOSS_TRANSIENT")
                }
                // Pause playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                if (DBG) {
                    Timber.d("AUDIOFOCUS_GAIN")
                }
                // Resume playback
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                if (DBG) {
                    Timber.d("AUDIOFOCUS_LOSS")
                }
                abandonAudioFocus()
            }
        }

    private val audioBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action ?: "") {
                Intent.ACTION_HEADSET_PLUG -> {
                    if (DBG) {
                        Timber.d("AudioBroadcastReceiver: ACTION_HEADSET_PLUG")
                        Timber.d("    state: %d", intent.getIntExtra("state", 0))
                        Timber.d("    name: %s", intent.getStringExtra("name"))
                    }
//                    val isHeadsetPlugged = intent.getIntExtra("state", 0) == 1
                    updateState()
                }
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    bluetoothHeadsetState = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_DISCONNECTED
                    )
                    if (DBG) {
                        Timber.d("AudioBroadcastReceiver: BLUETOOTH_HEADSET_STATE_CHANGED_ACTION")
                        Timber.d("==> new state: $bluetoothHeadsetState")
                    }

                    if (bluetoothHeadsetState == BluetoothHeadset.STATE_CONNECTED) {
                        tryConnectBluetooth()
                    } else if (bluetoothHeadsetState == BluetoothHeadset.STATE_DISCONNECTED) {
                        tryDisconnectBluetooth()
                    }

                    updateState()
                }
                BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED -> {
                    bluetoothHeadsetAudioState = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_STATE,
                        BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                    )
                    if (DBG) {
                        Timber.d("AudioBroadcastReceiver: BLUETOOTH_HEADSET_AUDIO_STATE_CHANGED_ACTION")
                        Timber.d("==> new state: $bluetoothHeadsetAudioState")
                    }
                    updateState()
                }
            }
        }
    }

    private val bluetoothProfileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceDisconnected(profile: Int) {
            Timber.d("Bluetooth -> onServiceDisconnected")
            bluetoothHeadset = null
            updateState()
        }

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            Timber.d("Bluetooth -> onServiceConnected")
            bluetoothHeadset = proxy as? BluetoothHeadset
            bluetoothHeadset?.run {
                val device = connectedDevices.firstOrNull() ?: run {
                    Timber.d("No connected devices")
                    return
                }
                bluetoothHeadsetState = getConnectionState(device)
                bluetoothHeadsetAudioState = if (isAudioConnected(device)) {
                    Timber.d("Bluetooth -> BluetoothHeadset.STATE_AUDIO_CONNECTED")
                    BluetoothHeadset.STATE_AUDIO_CONNECTED
                } else {
                    Timber.d("Bluetooth -> BluetoothHeadset.STATE_AUDIO_DISCONNECTED")
                    BluetoothHeadset.STATE_AUDIO_DISCONNECTED
                }

                tryConnectBluetooth()

                updateState()
            }
        }
    }

}