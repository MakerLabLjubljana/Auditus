package si.zascitimo.auditus.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.zascitimo.auditus.BuildConfig
import si.zascitimo.auditus.R
import si.zascitimo.auditus.RoundedDialogFragment
import si.zascitimo.auditus.databinding.FragmentInfoBinding

class InfoFragment : RoundedDialogFragment() {
    private lateinit var binding: FragmentInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInfoBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnClose.setOnClickListener { dismiss() }
        binding.tvVersion.text = getString(
            R.string.version,
            BuildConfig.VERSION_NAME
        )
    }
}