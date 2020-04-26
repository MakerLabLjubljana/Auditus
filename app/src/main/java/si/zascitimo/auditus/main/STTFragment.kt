package si.zascitimo.auditus.main

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import si.zascitimo.auditus.RoundedDialogFragment
import si.zascitimo.auditus.databinding.FragmentSttBinding
import timber.log.Timber


class STTFragment : RoundedDialogFragment() {
    private lateinit var binding: FragmentSttBinding

    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSttBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnClose.setOnClickListener { dismiss() }

        start()
    }

    override fun onStop() {
        speech?.stopListening()
        speech?.destroy()
        super.onStop()
    }

    private fun start() {
        speech?.destroy()
        val speech = SpeechRecognizer.createSpeechRecognizer(requireContext())
        this.speech = speech
        Timber.i(
            "isRecognitionAvailable: %s",
            SpeechRecognizer.isRecognitionAvailable(requireContext())
        )
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            speech.setRecognitionListener(listener)
        } else {
            dismiss()
        }

        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).also {
            it.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en")
            it.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            it.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        speech.startListening(recognizerIntent)
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Timber.d("onReadyForSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Timber.d("onRmsChanged")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Timber.d("onBufferReceived")
        }

        override fun onPartialResults(partialResults: Bundle?) {
            Timber.d("onPartialResults: %s", partialResults)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Timber.d("onEvent: %d", eventType)
        }

        override fun onBeginningOfSpeech() {
            Timber.d("onBeginningOfSpeech")
        }

        override fun onEndOfSpeech() {
            Timber.d("onEndOfSpeech")
            speech?.stopListening()
        }

        override fun onError(error: Int) {
            Timber.w("onError: %s", getErrorText(error))
            start()
        }

        override fun onResults(results: Bundle?) {
            val matches =
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?: emptyList<String>()
            val text = matches.joinToString(separator = "\n")
            Timber.d("onResults: %s", text)

            matches.firstOrNull()?.let {
                binding.txtDictation.append(it)
                binding.txtDictation.append(" ")
            }

            start()
        }

        fun getErrorText(errorCode: Int) = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
    }
}