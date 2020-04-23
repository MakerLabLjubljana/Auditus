//
// Created by zoki on 16.4.2020.
//

#include "AudioEngine.h"
#include "oboe-1.3/src/common/OboeDebug.h"

AudioEngine::AudioEngine() {

}

AudioEngine::~AudioEngine() {
    mFullDuplexPass.stop();
    closeStream(mPlayStream);
    closeStream(mRecordingStream);
}

void AudioEngine::start(int32_t recDeviceId, int32_t playDeviceId) {
    if (mIsStarted) {
        return;
    }

    mRecordingDeviceId = recDeviceId;
    mPlaybackDeviceId = playDeviceId;

    openStreams();
    mFullDuplexPass.start();

    mIsStarted = true;
}

void AudioEngine::stop() {
    if (!mIsStarted) {
        return;
    }

    mFullDuplexPass.stop();
    closeStream(mPlayStream);
    closeStream(mRecordingStream);

    mIsStarted = false;
}

void AudioEngine::openStreams() {
    oboe::AudioStreamBuilder inBuilder, outBuilder;
    setupPlaybackStreamParameters(&outBuilder);
    outBuilder.openStream(&mPlayStream);

    setupRecordingStreamParameters(&inBuilder);
    inBuilder.openStream(&mRecordingStream);

    mFullDuplexPass.setInputStream(mRecordingStream);
    mFullDuplexPass.setOutputStream(mPlayStream);
}

/**
 * Sets the stream parameters which are specific to recording,
 * including the sample rate which is determined from the
 * playback stream.
 *
 * @param builder The recording stream builder
 */
oboe::AudioStreamBuilder *AudioEngine::setupRecordingStreamParameters(
        oboe::AudioStreamBuilder *builder) {
    // This sample uses blocking read() by setting callback to null
    builder->setCallback(nullptr)
            ->setDeviceId(mRecordingDeviceId)
            ->setDirection(oboe::Direction::Input)
            ->setSampleRate(mSampleRate)
            ->setChannelCount(mInputChannelCount);
    return setupCommonStreamParameters(builder);
}

/**
 * Sets the stream parameters which are specific to playback, including device
 * id and the dataCallback function, which must be set for low latency
 * playback.
 * @param builder The playback stream builder
 */
oboe::AudioStreamBuilder *AudioEngine::setupPlaybackStreamParameters(
        oboe::AudioStreamBuilder *builder) {
    builder->setCallback(this)
            ->setDeviceId(mPlaybackDeviceId)
            ->setDirection(oboe::Direction::Output)
            ->setChannelCount(mOutputChannelCount);

    return setupCommonStreamParameters(builder);
}

/**
 * Set the stream parameters which are common to both recording and playback
 * streams.
 * @param builder The playback or recording stream builder
 */
oboe::AudioStreamBuilder *AudioEngine::setupCommonStreamParameters(
        oboe::AudioStreamBuilder *builder) {
    // We request EXCLUSIVE mode since this will give us the lowest possible
    // latency.
    // If EXCLUSIVE mode isn't available the builder will fall back to SHARED
    // mode.
    builder->setFormat(mFormat)
//            ->setAudioApi(mAudioApi)
            ->setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency);
    return builder;
}


/**
 * Close the stream. AudioStream::close() is a blocking call so
 * the application does not need to add synchronization between
 * onAudioReady() function and the thread calling close().
 * [the closing thread is the UI thread in this sample].
 * @param stream the stream to close
 */
void AudioEngine::closeStream(oboe::AudioStream *stream) {
    if (stream) {
        oboe::Result result = stream->close();
        if (result != oboe::Result::OK) {
            LOGE("Error closing stream. %s", oboe::convertToText(result));
        }
        LOGW("Successfully closed streams");
    }
}

/**
 * Handles playback stream's audio request. In this sample, we simply block-read
 * from the record stream for the required samples.
 *
 * @param oboeStream: the playback stream that requesting additional samples
 * @param audioData:  the buffer to load audio samples for playback stream
 * @param numFrames:  number of frames to load to audioData buffer
 * @return: DataCallbackResult::Continue.
 */
oboe::DataCallbackResult AudioEngine::onAudioReady(
        oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
    return mFullDuplexPass.onAudioReady(oboeStream, audioData, numFrames);
}

/**
 * Oboe notifies the application for "about to close the stream".
 *
 * @param oboeStream: the stream to close
 * @param error: oboe's reason for closing the stream
 */
void AudioEngine::onErrorBeforeClose(oboe::AudioStream *oboeStream,
                                          oboe::Result error) {
    LOGE("%s stream Error before close: %s",
         oboe::convertToText(oboeStream->getDirection()),
         oboe::convertToText(error));
}

/**
 * Oboe notifies application that "the stream is closed"
 *
 * @param oboeStream
 * @param error
 */
void AudioEngine::onErrorAfterClose(oboe::AudioStream *oboeStream,
                                         oboe::Result error) {
    LOGE("%s stream Error after close: %s",
         oboe::convertToText(oboeStream->getDirection()),
         oboe::convertToText(error));
}

