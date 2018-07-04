package com.test.screenimage.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.test.screenimage.configuration.AudioConfiguration;


public class AudioUtils {
//    public static boolean checkMicSupport(AudioConfiguration audioConfiguration) {
//        boolean result;
//        int recordBufferSize = getRecordBufferSize(audioConfiguration);
//        byte[] mRecordBuffer = new byte[recordBufferSize];
//        AudioRecord audioRecord = getAudioRecord(audioConfiguration);
//        try {
//            audioRecord.startRecording();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        int readLen = audioRecord.read(mRecordBuffer, 0, recordBufferSize);
//        result = readLen >= 0;
//        try {
//            audioRecord.release();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    public static int getRecordBufferSize(AudioConfiguration audioConfiguration) {
        int frequency = audioConfiguration.frequency;
        int audioEncoding = audioConfiguration.encoding;
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if(audioConfiguration.channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        int size = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        return size;
    }

    @TargetApi(18)
    public static AudioRecord getAudioRecord(AudioConfiguration audioConfiguration,Context context) {
        int frequency = audioConfiguration.frequency;
        int audioEncoding = audioConfiguration.encoding;
        //单声道(定义采样通道)
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        if(audioConfiguration.channelCount == 2) {
            channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        }
        //音频源:麦克风
        int audioSource = MediaRecorder.AudioSource.MIC;
        if(audioConfiguration.aec) {
            //对麦克风中类似ip通话的交流声音进行识别，默认会开启回声消除和自动增益
            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        }
        // TODO: 2018/6/5 wt 消除回声
        AudioManager audioManager = (AudioManager)context.getSystemService(context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0,
                AudioManager.STREAM_VOICE_CALL);
        //音频源，采样率，声道数，采样位数，缓冲区大小
        Log.e("wtt", "getAudioRecord: "+ getRecordBufferSize(audioConfiguration) );
        AudioRecord audioRecord = new AudioRecord(audioSource, frequency,
                channelConfiguration, audioEncoding, getRecordBufferSize(audioConfiguration));
        return audioRecord;
    }
}
