package si.zascitimo.auditus.main

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import si.zascitimo.auditus.App
import si.zascitimo.auditus.audio.AudioService
import si.zascitimo.auditus.databinding.ActivityMainBinding
import si.zascitimo.auditus.prefs
import si.zascitimo.auditus.settings.SettingsActivity
import si.zascitimo.auditus.welcome.WelcomeActivity
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val audioStatus by lazy { (application as App).audioStatus }
    private val audioRouter by lazy { (application as App).audioRouter }

    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.showWelcome) {
            startActivity(
                Intent(
                    this,
                    WelcomeActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Timber.d("Start")

        binding.btnInfo.setOnClickListener {
            InfoFragment()
                .show(supportFragmentManager, "info")
        }

        binding.btnPlay.setOnClickListener {
            startService(
                AudioService.startIntent(
                    this
                )
            )
        }

        binding.btnPause.setOnClickListener {
            startService(
                AudioService.stopIntent(
                    this
                )
            )
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnDictation.setOnClickListener {
            STTFragment()
                .show(supportFragmentManager, "stt")
        }

        binding.btnBtSettings.setOnClickListener {
            if (bluetoothAdapter != null) {
//                val filter = IntentFilter("android.bluetooth.devicepicker.action.DEVICE_SELECTED")
//                registerReceiver(object : BroadcastReceiver() {
//                    override fun onReceive(context: Context?, intent: Intent?) {
//                        val btDevice = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice
//                        btDevice?.createBond()
//                        Timber.d("onReceive")
//                    }
//
//                }, filter)
//                try {
//                    startActivity(Intent("android.bluetooth.devicepicker.action.LAUNCH"))
////                    startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
//                } catch (e: Exception) {
//                    try {
//                        startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
//                    } catch (e: Exception) {
//                    }
//                }
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
                } catch (e: Exception) {
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
                    binding.btnDictation.visibility = View.GONE
                }
                it.missingPlaybackDevice -> {
                    binding.groupNoMic.visibility = View.GONE
                    binding.imgNoPlayback.visibility = View.VISIBLE
                    binding.imgInternalSpeaker.visibility = View.GONE
                    binding.imgWiredSpeaker.visibility = View.GONE
                    binding.btnPlay.visibility = View.GONE
                    binding.btnPause.visibility = View.GONE
                    binding.btnDictation.visibility = View.GONE
                }
                else -> {
                    binding.groupNoMic.visibility = View.GONE
                    binding.imgNoPlayback.visibility = View.GONE
                    binding.btnDictation.visibility = View.VISIBLE
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
                            binding.btnPlay.visibility = View.INVISIBLE
                            binding.btnPause.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.btnPlay.visibility = View.VISIBLE
                            binding.btnPause.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        })

        audioRouter.audioDeviceState.observe(this, Observer {
//            binding.audioDeviceState.text = it.toString()
        })

        startService(AudioService.initIntent(this))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
