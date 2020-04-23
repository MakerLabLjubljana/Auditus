#include <jni.h>
#include <string>
#include <oboe/Definitions.h>
#include "AudioEngine.h"

static AudioEngine *audioEngine = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_si_zascitimo_auditus_NativeInterfaceKt_createAudioEngine(JNIEnv *env, jclass clazz) {
    if (audioEngine == nullptr) {
        audioEngine = new AudioEngine();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_si_zascitimo_auditus_NativeInterfaceKt_setDefaultStreamValues(JNIEnv *env, jclass clazz,
                                                                  jint default_sample_rate,
                                                                  jint default_frames_per_burst) {
    oboe::DefaultStreamValues::SampleRate = (int32_t) default_sample_rate;
    oboe::DefaultStreamValues::FramesPerBurst = (int32_t) default_frames_per_burst;
}

extern "C"
JNIEXPORT void JNICALL
Java_si_zascitimo_auditus_NativeInterfaceKt_startStream(JNIEnv *env, jclass clazz, jint record_device_id,
                                                       jint play_device_id) {
    audioEngine->start(record_device_id, play_device_id);
}
extern "C"
JNIEXPORT void JNICALL
Java_si_zascitimo_auditus_NativeInterfaceKt_stopStream(JNIEnv *env, jclass clazz) {
    audioEngine->stop();
}
