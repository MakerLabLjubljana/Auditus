package si.zascitimo.auditus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class InfoFragment : RoundedDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = layoutInflater.inflate(R.layout.fragment_info, container, false)
        v.findViewById<View>(R.id.btnClose).setOnClickListener { dismiss() }
        v.findViewById<TextView>(R.id.tvVersion).text = getString(R.string.version, BuildConfig.VERSION_NAME)
        return v
    }
}