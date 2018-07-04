package com.test.screenimage.mediacodec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.constant.Constants;
import com.test.screenimage.utils.BlackListHelper;
import com.test.screenimage.utils.SopCastLog;


@TargetApi(18)
public class VideoMediaCodec {

    public static MediaCodec getVideoMediaCodec(VideoConfiguration videoConfiguration) {
        int videoWidth = getVideoSize(videoConfiguration.width);
        int videoHeight = getVideoSize(videoConfiguration.height);
        if (Build.MANUFACTURER.equalsIgnoreCase("XIAOMI")) {
            videoConfiguration.maxBps = 500;
            videoConfiguration.fps = 10;
            videoConfiguration.ifi = 3;
        }
        MediaFormat format = MediaFormat.createVideoFormat(videoConfiguration.mime, videoWidth, videoHeight);
        //设置颜色格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //设置比特率
        format.setInteger(MediaFormat.KEY_BIT_RATE, videoConfiguration.maxBps * 1024);
        int fps = videoConfiguration.fps;
        //设置摄像头预览帧率
        if (BlackListHelper.deviceInFpsBlacklisted()) {
            SopCastLog.d(Constants.TAG, "Device in fps setting black list, so set mediacodec fps 15");
            fps = 15;
        }
        //设置帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
        //关键帧间隔时间，通常情况下，你设置成多少问题都不大。
        //比如你设置成10，那就是10秒一个关键帧。但是，如果你有需求要做视频的预览，那你最好设置成1
        //因为如果你设置成10，那你会发现，10秒内的预览都是一个截图
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, videoConfiguration.ifi);
        // -----------------ADD BY XU.WANG 当画面静止时,重复最后一帧--------------------------------------------------------
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, 1000000 / 45);
        //------------------MODIFY BY XU.WANG 为解决MIUI9.5花屏而增加...-------------------------------
        if (Build.MANUFACTURER.equalsIgnoreCase("XIAOMI")) {
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CQ);
        } else {
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
        }
        format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
        MediaCodec mediaCodec = null;

        try {
            mediaCodec = MediaCodec.createEncoderByType(videoConfiguration.mime);
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (Exception e) {
            e.printStackTrace();
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
            }
        }
        return mediaCodec;
    }

    // We avoid the device-specific limitations on width and height by using values that
    // are multiples of 16, which all tested devices seem to be able to handle.
    public static int getVideoSize(int size) {
        int multiple = (int) Math.ceil(size / 16.0);
        return multiple * 16;
    }
}
