package si.zascitimo.auditus

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import com.example.auditus.AudioRouter
import si.zascitimo.auditus.audio.ActiveDevices
import si.zascitimo.auditus.audio.AudioStatus
import timber.log.Timber

val prefs: Prefs by lazy {
    App.prefs
}

class App : Application() {

    val audioRouter by lazy { AudioRouter(applicationContext) }

    val audioStatus = MediatorLiveData<AudioStatus>()

    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("native-lib")

        Timber.plant(Timber.DebugTree())

        prefs = Prefs(applicationContext)

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

    companion object {
        lateinit var prefs: Prefs
    }
}