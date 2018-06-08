package com.test.screenimage.audio;

import android.media.MediaCodec;

import java.nio.ByteBuffer;


public interface OnAudioEncodeListener {
    void onAudioEncode(ByteBuffer bb, MediaCodec.BufferInfo bi);
}
