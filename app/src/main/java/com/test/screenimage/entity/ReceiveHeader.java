package com.test.screenimage.entity;

/**
 * Created by wt on 2018/6/12.
 * 解析出协议数据头格式
 * mainCmd 主指令
 * subCmd 子指令
 * encodeVersion 编码版本
 * stringBodylength 文本消息内容长度
 * buffSize 音视频内容长度
 */
public class ReceiveHeader {
    private int mainCmd;
    private int subCmd;
    private byte encodeVersion;
    private int stringBodylength;
    private int buffSize;

    public ReceiveHeader(int mainCmd, int subCmd, byte encodeVersion, int stringBodylength, int buffSize) {
        this.mainCmd = mainCmd;
        this.subCmd = subCmd;
        this.encodeVersion = encodeVersion;
        this.stringBodylength = stringBodylength;
        this.buffSize = buffSize;
    }

    public int getMainCmd() {
        return mainCmd;
    }

    public void setMainCmd(int mainCmd) {
        this.mainCmd = mainCmd;
    }

    public int getSubCmd() {
        return subCmd;
    }

    public void setSubCmd(int subCmd) {
        this.subCmd = subCmd;
    }

    public byte getEncodeVersion() {
        return encodeVersion;
    }

    public void setEncodeVersion(byte encodeVersion) {
        this.encodeVersion = encodeVersion;
    }

    public int getStringBodylength() {
        return stringBodylength;
    }

    public void setStringBodylength(int stringBodylength) {
        this.stringBodylength = stringBodylength;
    }

    public int getBuffSize() {
        return buffSize;
    }

    public void setBuffSize(int buffSize) {
        this.buffSize = buffSize;
    }
}
