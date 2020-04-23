package si.zascitimo.auditus

external fun createAudioEngine()

external fun setDefaultStreamValues(defaultSampleRate: Int, defaultFramesPerBurst: Int)

external fun startStream(recordDeviceId: Int, playDeviceId: Int)

external fun stopStream()
