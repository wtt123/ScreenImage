package com.test.screenimage.entity;

/**
 * Created by wt on 2018/6/12.
 * 解析后的一组数据
 */
public class ReceiveData {
    private ReceiveHeader header;
    private String sendBody;
    private byte[] buff;

    public ReceiveData() {
    }

    /**
     * @param header 协议头
     * @param sendBody 文本内容
     * @param buff 数组内容
     */
    public ReceiveData(ReceiveHeader header, String sendBody, byte[] buff) {
        this.header = header;
        this.sendBody = sendBody;
        this.buff = buff;
    }

    public ReceiveHeader getHeader() {
        return header;
    }

    public void setHeader(ReceiveHeader header) {
        this.header = header;
    }

    public String getSendBody() {
        return sendBody;
    }

    public void setSendBody(String sendBody) {
        this.sendBody = sendBody;
    }

    public byte[] getBuff() {
        return buff;
    }

    public void setBuff(byte[] buff) {
        this.buff = buff;
    }
}

