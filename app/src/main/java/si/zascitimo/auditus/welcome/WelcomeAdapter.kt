package si.zascitimo.auditus.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import si.zascitimo.auditus.R

class WelcomeAdapter(
    private val goNext: (Int) -> Unit,
    private val openSettings: () -> Unit,
    private val finish: () -> Unit
) : RecyclerView.Adapter<WelcomeAdapter.ViewHolder>() {

    class ViewHolder(
        itemView: View, private val goNext: (Int) -> Unit,
        private val openSettings: () -> Unit,
        private val finish: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<View>(R.id.btnGetStarted)?.setOnClickListener {
                goNext(1)
            }
            itemView.findViewById<View>(R.id.btnBtSettings)?.setOnClickListener {
                openSettings()
                goNext(2)
            }
            itemView.findViewById<View>(R.id.btnStart)?.setOnClickListener {
                finish()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(viewType, parent, false)
        return ViewHolder(v, goNext, openSettings, finish)
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> R.layout.welcome_about
        1 -> R.layout.welcome_link
        2 -> R.layout.welcome_setup
        else -> R.layout.welcome_about
    }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }
}