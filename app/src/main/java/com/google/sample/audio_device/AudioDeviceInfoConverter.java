package com.google.sample.audio_device;
/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.media.AudioDeviceInfo;

public class AudioDeviceInfoConverter {

    /**
     * Converts an {@link AudioDeviceInfo} object into a human readable representation
     *
     * @param adi The AudioDeviceInfo object to be converted to a String
     * @return String containing all the information from the AudioDeviceInfo object
     */
    public static String toString(AudioDeviceInfo adi) {

        StringBuilder sb = new StringBuilder();
        sb.append("Id: ");
        sb.append(adi.getId());

        sb.append("\nProduct name: ");
        sb.append(adi.getProductName());

        sb.append("\nType: ");
        sb.append(typeToString(adi.getType()));

        sb.append("\nIs source: ");
        sb.append((adi.isSource() ? "Yes" : "No"));

        sb.append("\nIs sink: ");
        sb.append((adi.isSink() ? "Yes" : "No"));

        sb.append("\nChannel counts: ");
        int[] channelCounts = adi.getChannelCounts();
        sb.append(intArrayToString(channelCounts));

        sb.append("\nChannel masks: ");
        int[] channelMasks = adi.getChannelMasks();
        sb.append(intArrayToString(channelMasks));

        sb.append("\nChannel index masks: ");
        int[] channelIndexMasks = adi.getChannelIndexMasks();
        sb.append(intArrayToString(channelIndexMasks));

        sb.append("\nEncodings: ");
        int[] encodings = adi.getEncodings();
        sb.append(intArrayToString(encodings));

        sb.append("\nSample Rates: ");
        int[] sampleRates = adi.getSampleRates();
        sb.append(intArrayToString(sampleRates));

        return sb.toString();
    }

    /**
     * Converts an integer array into a string where each int is separated by a space
     *
     * @param integerArray the integer array to convert to a string
     * @return string containing all the integer values separated by spaces
     */
    private static String intArrayToString(int[] integerArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < integerArray.length; i++) {
            sb.append(integerArray[i]);
            if (i != integerArray.length - 1) sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * Converts the value from {@link AudioDeviceInfo#getType()} into a human
     * readable string
     *
     * @param type One of the {@link AudioDeviceInfo}.TYPE_* values
     *             e.g. AudioDeviceInfo.TYPE_BUILT_IN_SPEAKER
     * @return string which describes the type of audio device
     */
    public static String typeToString(int type) {
        switch (type) {
            case AudioDeviceInfo.TYPE_AUX_LINE:
                return "Aux Line";
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
                return "Bluetooth A2DP Controls";
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
                return "Bluetooth Device";
            case AudioDeviceInfo.TYPE_BUILTIN_EARPIECE:
                return "Built-in Earphone";
            case AudioDeviceInfo.TYPE_BUILTIN_MIC:
                return "Built-in Microphone";
            case AudioDeviceInfo.TYPE_BUILTIN_SPEAKER:
                return "Built-in Speaker";
            case AudioDeviceInfo.TYPE_BUS:
                return "BUS";
            case AudioDeviceInfo.TYPE_DOCK:
                return "DOCK";
            case AudioDeviceInfo.TYPE_FM:
                return "FM";
            case AudioDeviceInfo.TYPE_FM_TUNER:
                return "FM Tuner";
            case AudioDeviceInfo.TYPE_HDMI:
                return "HDMI";
            case AudioDeviceInfo.TYPE_HDMI_ARC:
                return "HDMI Audio Return Channel";
            case AudioDeviceInfo.TYPE_IP:
                return "IP";
            case AudioDeviceInfo.TYPE_LINE_ANALOG:
                return "Analog Line";
            case AudioDeviceInfo.TYPE_LINE_DIGITAL:
                return "Digital Line";
            case AudioDeviceInfo.TYPE_TELEPHONY:
                return "Telephony Device";
            case AudioDeviceInfo.TYPE_TV_TUNER:
                return "TV Tuner";
            case AudioDeviceInfo.TYPE_USB_ACCESSORY:
                return "USB Accessory";
            case AudioDeviceInfo.TYPE_USB_DEVICE:
                return "USB Device";
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
                return "Wired Headphones";
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
                return "Wired Headset";
            default:
            case AudioDeviceInfo.TYPE_UNKNOWN:
                return "unknown";
        }
    }
}
