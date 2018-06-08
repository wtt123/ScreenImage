package com.test.screenimage.entity;

// TODO: 2018/5/29 wt修改Frame<T>
public class Frame {
    public static final int FRAME_TYPE_AUDIO = 1;
    public static final int FRAME_TYPE_KEY_FRAME = 2;
    public static final int FRAME_TYPE_INTER_FRAME = 3;
    public static final int FRAME_TYPE_CONFIGURATION = 4;

    //    public T data;
    public byte[] data;
    public int packetType;
    public int frameType;

    public Frame(byte[] data, int packetType, int frameType) {
        this.data = data;
        this.packetType = packetType;
        this.frameType = frameType;
    }
}
