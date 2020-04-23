//
// Created by zoki on 16.4.2020.
//

#ifndef AUDITUS_AUDIOENGINE_H
#define AUDITUS_AUDIOENGINE_H

#include <jni.h>
#include <oboe/Oboe.h>
#include <string>
#include <thread>
#include "FullDuplexPass.h"

class AudioEngine : public oboe::AudioStreamCallback {
public:
    AudioEngine();

    ~AudioEngine();

    void start(int32_t recDeviceId, int32_t playDeviceId);
    void stop();

    void openStreams();

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *oboeStream,
                                          void *audioData, int32_t numFrames);

    void onErrorBeforeClose(oboe::AudioStream *oboeStream, oboe::Result error);

    void onErrorAfterClose(oboe::AudioStream *oboeStream, oboe::Result error);

private:
    FullDuplexPass mFullDuplexPass;

    bool mIsStarted = false;

    int32_t mRecordingDeviceId = oboe::kUnspecified;
    int32_t mPlaybackDeviceId = oboe::kUnspecified;
    oboe::AudioFormat mFormat = oboe::AudioFormat::I16;
    int32_t mSampleRate = oboe::kUnspecified;
    int32_t mInputChannelCount = oboe::ChannelCount::Stereo;
    int32_t mOutputChannelCount = oboe::ChannelCount::Stereo;

    oboe::AudioStream *mRecordingStream = nullptr;
    oboe::AudioStream *mPlayStream = nullptr;

    void closeStream(oboe::AudioStream *stream);

    oboe::AudioStreamBuilder *setupCommonStreamParameters(
            oboe::AudioStreamBuilder *builder);
    oboe::AudioStreamBuilder *setupRecordingStreamParameters(
            oboe::AudioStreamBuilder *builder);
    oboe::AudioStreamBuilder *setupPlaybackStreamParameters(
            oboe::AudioStreamBuilder *builder);
};


#endif //AUDITUS_AUDIOENGINE_H
