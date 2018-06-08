package com.test.screenimage.controller;

import android.media.MediaCodec;

import com.test.screenimage.audio.OnAudioEncodeListener;
import com.test.screenimage.configuration.AudioConfiguration;
import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.controller.audio.IAudioController;
import com.test.screenimage.controller.video.IVideoController;
import com.test.screenimage.stream.packer.Packer;
import com.test.screenimage.stream.sender.Sender;
import com.test.screenimage.stream.sender.tcp.TcpSender;
import com.test.screenimage.utils.SopCastUtils;
import com.test.screenimage.video.OnVideoEncodeListener;

import java.nio.ByteBuffer;

/**
 * @Title: StreamController
 * @Package com.laifeng.sopcastsdk.controller
 * @Description:
 * @Author Jim
 * @Date 16/9/14
 * @Time 上午11:44
 * @Version
 */
public class StreamController implements OnAudioEncodeListener, OnVideoEncodeListener, Packer.OnPacketListener {
    private Packer mPacker;
    private Sender mSender;
    private IVideoController mVideoController;
    private IAudioController mAudioController;

    public StreamController(IVideoController videoProcessor, IAudioController audioProcessor) {
        mAudioController = audioProcessor;
        mVideoController = videoProcessor;
    }

    public void setVideoConfiguration(VideoConfiguration videoConfiguration) {
        mVideoController.setVideoConfiguration(videoConfiguration);
    }


    public void setPacker(Packer packer) {
        mPacker = packer;
        //设置打包监听器
        mPacker.setPacketListener(this);
    }

    public void setSender(Sender sender) {
        mSender = sender;
    }

    public synchronized void start() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                if (mPacker == null) {
                    return;
                }
                if (mSender == null) {
                    return;
                }
                //开始打包
                mPacker.start();
                //开始发送
                mSender.start();
                mVideoController.setVideoEncoderListener(StreamController.this);
                mVideoController.start();
                if (mAudioController != null) {
                    mAudioController.setAudioEncodeListener(StreamController.this);
                    mAudioController.start();
                }
            }
        });
    }

    public synchronized void stop() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                if (mAudioController != null) {
                    mAudioController.setAudioEncodeListener(null);
                    mAudioController.stop();
                }
                mVideoController.setVideoEncoderListener(null);
                mVideoController.stop();
                if (mSender != null) {
                    mSender.stop();
                }
                if (mPacker != null) {
                    mPacker.stop();
                }
            }
        });
    }

    public synchronized void pause() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                if (mAudioController != null) mAudioController.pause();
                mVideoController.pause();
            }
        });
    }

    public synchronized void resume() {
        SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
            @Override
            public void process() {
                if (mAudioController != null) mAudioController.resume();
                mVideoController.resume();
            }
        });
    }

    public void mute(boolean mute) {
        if (mAudioController != null) mAudioController.mute(mute);
    }

    public int getSessionId() {
        return mAudioController == null ? 0 : mAudioController.getSessionId();
    }

    public boolean setVideoBps(int bps) {
        return mVideoController.setVideoBps(bps);
    }

    @Override
    public void onAudioEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        if (mPacker != null) {
            mPacker.onAudioData(bb, bi);
        }
    }

    @Override
    public void onVideoEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
        if (mPacker != null) {
            mPacker.onVideoData(bb, bi);
        }
    }

    @Override
    public void onPacket(byte[] data, int packetType) {
        if (mSender != null) {
            mSender.onData(data, packetType);
        }
    }

    @Override
    public void onSpsPps(byte[] mSpsPps) {
        if (mSender != null && mSender instanceof TcpSender) {
            ((TcpSender) mSender).setSpsPps(mSpsPps);
        }
    }
}
