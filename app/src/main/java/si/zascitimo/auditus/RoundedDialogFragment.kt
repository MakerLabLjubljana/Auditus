package si.zascitimo.auditus

import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

open class RoundedDialogFragment : DialogFragment() {
    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun getTheme(): Int = R.style.DialogTheme
}