package si.zascitimo.auditus

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.example.auditus.AudioRouter
import timber.log.Timber

class App : Application() {

    val audioRouter by lazy { AudioRouter(applicationContext) }

    val audioStatus = MediatorLiveData<AudioStatus>()

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("native-lib")

        Timber.plant(Timber.DebugTree())

        audioStatus.addSource(audioRouter.activeDevice) {
            audioStatus.value = AudioStatus(
                !it.hasRecordingDevice(),
                !it.hasPlaybackDevice(),
                audioRouter.isStreamActive.value ?: false,
                it.wiredSpeaker == null
            )
        }
        audioStatus.addSource(audioRouter.isStreamActive) {
            val activeDevice = audioRouter.activeDevice.value ?: ActiveDevices()
            audioStatus.value = AudioStatus(
                !activeDevice.hasRecordingDevice(),
                !activeDevice.hasPlaybackDevice(),
                it,
                activeDevice.wiredSpeaker == null
            )
        }
    }
}