package com.test.screenimage.controller.video;

import com.test.screenimage.configuration.VideoConfiguration;
import com.test.screenimage.video.OnVideoEncodeListener;

public interface IVideoController {
    void start();
    void stop();
    void pause();
    void resume();
    boolean setVideoBps(int bps);
    void setVideoEncoderListener(OnVideoEncodeListener listener);
    void setVideoConfiguration(VideoConfiguration configuration);
}
