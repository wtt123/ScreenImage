package com.test.screenimage.controller.audio;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.util.Log;

import com.test.screenimage.audio.AudioProcessor;
import com.test.screenimage.audio.OnAudioEncodeListener;
import com.test.screenimage.configuration.AudioConfiguration;
import com.test.screenimage.constant.Constants;
import com.test.screenimage.utils.AudioUtils;
import com.test.screenimage.utils.SopCastLog;


/**
 * @Title: NormalAudioController
 * @Package com.laifeng.sopcastsdk.controller.audio
 * @Description:
 * @Author Jim
 * @Date 16/9/14
 * @Time 下午12:53
 * @Version
 */
public class NormalAudioController implements IAudioController {
    private OnAudioEncodeListener mListener;
    private AudioRecord mAudioRecord;
    private AudioProcessor mAudioProcessor;
    // TODO: 2018/6/5 增加上下文
    private Context mContext;
    private boolean mMute;
    private AudioConfiguration mAudioConfiguration;

    public NormalAudioController(Context context) {
        this.mContext=context;
        mAudioConfiguration = AudioConfiguration.createDefault();
    }

    public void setAudioConfiguration(AudioConfiguration audioConfiguration) {
        mAudioConfiguration = audioConfiguration;
    }

    public void setAudioEncodeListener(OnAudioEncodeListener listener) {
        mListener = listener;
    }

    public void start() {
        SopCastLog.d(Constants.TAG, "Audio Recording start");
        mAudioRecord = AudioUtils.getAudioRecord(mAudioConfiguration,mContext);
        try {
            //音频录制
            mAudioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAudioProcessor = new AudioProcessor(mAudioRecord, mAudioConfiguration);
        mAudioProcessor.setAudioHEncodeListener(mListener);
        mAudioProcessor.start();
        mAudioProcessor.setMute(mMute);
    }

    public void stop() {
        SopCastLog.d(Constants.TAG, "Audio Recording stop");
        if(mAudioProcessor != null) {
            mAudioProcessor.stopEncode();
        }
        if(mAudioRecord != null) {
            try {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        SopCastLog.d(Constants.TAG, "Audio Recording pause");
        try {
            if (mAudioRecord != null) {
                mAudioRecord.stop();
            }
            if (mAudioProcessor != null) {
                mAudioProcessor.pauseEncode(true);
            }
        }catch (Exception e){
            Log.e("NormalAudioController","" + e.toString());
        }
    }

    public void resume() {
        SopCastLog.d(Constants.TAG, "Audio Recording resume");
        if(mAudioRecord != null) {
            mAudioRecord.startRecording();
        }
        if (mAudioProcessor != null) {
            mAudioProcessor.pauseEncode(false);
        }
    }

    public void mute(boolean mute) {
        SopCastLog.d(Constants.TAG, "Audio Recording mute: " + mute);
        mMute = mute;
        if(mAudioProcessor != null) {
            mAudioProcessor.setMute(mMute);
        }
    }

    @Override
    @TargetApi(16)
    public int getSessionId() {
        if(mAudioRecord != null) {
            return mAudioRecord.getAudioSessionId();
        } else {
            return -1;
        }
    }
}
