package si.zascitimo.auditus

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import si.zascitimo.auditus.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val audioStatus by lazy { (application as App).audioStatus }
    private val audioRouter by lazy { (application as App).audioRouter }

    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Timber.d("Start")

        binding.btnInfo.setOnClickListener {
            InfoFragment().show(supportFragmentManager, "info")
        }

        binding.btnPlay.setOnClickListener {
            startService(AudioService.startIntent(this))
        }

        binding.btnPause.setOnClickListener {
            startService(AudioService.stopIntent(this))
        }

        binding.btnSettings.setOnClickListener {
        }

        binding.btnBtSettings.setOnClickListener {
            if (bluetoothAdapter != null) {
                try {
                    startActivity(Intent("android.bluetooth.devicepicker.action.LAUNCH"))
                } catch (e: Exception) {
                    try {
                        startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (e: Exception) {
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
//            binding.statusText.text = getString(R.string.missing_audio_permission)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        audioStatus.observe(this, Observer {
            when {
                it.missingBtDevice -> {
                    binding.groupNoMic.visibility = View.VISIBLE
                    binding.imgNoPlayback.visibility = View.GONE
                    binding.imgInternalSpeaker.visibility = View.GONE
                    binding.imgWiredSpeaker.visibility = View.GONE
                    binding.btnPlay.visibility = View.GONE
                    binding.btnPause.visibility = View.GONE
                }
                it.missingPlaybackDevice -> {
                    binding.groupNoMic.visibility = View.GONE
                    binding.imgNoPlayback.visibility = View.VISIBLE
                    binding.imgInternalSpeaker.visibility = View.GONE
                    binding.imgWiredSpeaker.visibility = View.GONE
                    binding.btnPlay.visibility = View.GONE
                    binding.btnPause.visibility = View.GONE
                }
                else -> {
                    binding.groupNoMic.visibility = View.GONE
                    binding.imgNoPlayback.visibility = View.GONE
                    when {
                        it.inInternalSpeaker -> {
                            binding.imgInternalSpeaker.visibility = View.VISIBLE
                            binding.imgWiredSpeaker.visibility = View.GONE
                        }
                        else -> {
                            binding.imgInternalSpeaker.visibility = View.GONE
                            binding.imgWiredSpeaker.visibility = View.VISIBLE
                        }
                    }
                    when {
                        it.isStreamActive -> {
                            binding.btnPlay.visibility = View.GONE
                            binding.btnPause.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.btnPlay.visibility = View.VISIBLE
                            binding.btnPause.visibility = View.GONE
                        }
                    }
                }
            }
        })

        audioRouter.audioDeviceState.observe(this, Observer {
//            binding.audioDeviceState.text = it.toString()
        })

        startService(AudioService.initIntent(this))

//        binding.recordingDevicesSpinner.setDirectionType(AudioManager.GET_DEVICES_INPUTS)
//        binding.recordingDevicesSpinner.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onItemSelected(
//                adapterView: AdapterView<*>?,
//                view: View,
//                i: Int,
//                l: Long
//            ) {
//                audioRouter.customRecordingDevice =
//                    (binding.recordingDevicesSpinner.selectedItem as AudioDeviceListEntry?)?.id
//            }
//
//            override fun onNothingSelected(adapterView: AdapterView<*>?) { // Do nothing
//            }
//        }
//
//        binding.playbackDevicesSpinner.setDirectionType(AudioManager.GET_DEVICES_OUTPUTS)
//        binding.playbackDevicesSpinner.onItemSelectedListener = object : OnItemSelectedListener {
//            override fun onItemSelected(
//                adapterView: AdapterView<*>?,
//                view: View,
//                i: Int,
//                l: Long
//            ) {
//                audioRouter.customPlaybackDevice =
//                    (binding.playbackDevicesSpinner.selectedItem as AudioDeviceListEntry?)?.id
//            }
//
//            override fun onNothingSelected(adapterView: AdapterView<*>?) { // Do nothing
//            }
//        }
    }

    override fun onStart() {
        super.onStart()

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 2)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val firstResult = grantResults.firstOrNull() ?: return
        if (firstResult != PackageManager.PERMISSION_GRANTED) {
//            binding.statusText.text = getString(R.string.missing_audio_permission)
        } else {
//            binding.statusText.text = ""
        }
    }
}
