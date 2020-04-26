package si.zascitimo.auditus.welcome

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import si.zascitimo.auditus.databinding.ActivityWelcomeBinding
import si.zascitimo.auditus.prefs

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pager.adapter = WelcomeAdapter(
            goNext = {
                binding.pager.currentItem = it
            },
            openSettings = {
                try {
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                } catch (e: Exception) {
                }
            },
            finish = {
                prefs.showWelcome = false
                finish()
            }
        )
        binding.indicator.setViewPager(binding.pager)
    }
}
