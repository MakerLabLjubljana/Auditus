package si.zascitimo.auditus

import android.content.Context
import androidx.preference.PreferenceManager

class Prefs(context: Context) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    var showWelcome: Boolean
        get() = prefs.getBoolean("welcome", true)
        set(value) = prefs.edit().putBoolean("welcome", value).apply()

    var recordDevice: Int
        get() = prefs.getInt("recording", -1)
        set(value) = prefs.edit().putInt("recording", value).apply()

    var playbackDevice: Int
        get() = prefs.getInt("playback", -1)
        set(value) = prefs.edit().putInt("playback", value).apply()
}