package com.test.screenimage.controller.audio;

import com.test.screenimage.audio.OnAudioEncodeListener;
import com.test.screenimage.configuration.AudioConfiguration;

public interface IAudioController {
    void start();
    void stop();
    void pause();
    void resume();
    void mute(boolean mute);
    int getSessionId();
    void setAudioEncodeListener(OnAudioEncodeListener listener);
}
