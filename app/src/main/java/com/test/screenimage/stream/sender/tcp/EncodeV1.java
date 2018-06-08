package com.test.screenimage.stream.sender.tcp;

import com.test.screenimage.utils.ByteUtil;

import java.nio.ByteBuffer;

/**
 * Created by wt
 * Date on  2018/6/4  .
 *
 */

public class EncodeV1 {
    private byte[] buff;    //要发送的内容

    public EncodeV1(byte[] buff) {
        this.buff = buff;
    }

    public byte[] buildSendContent() {
        if (buff == null || buff.length == 0) {
            return null;
        }
        //创建一个4 + buff.length内存缓冲区
        ByteBuffer bb = ByteBuffer.allocate(4 + buff.length);
        bb.put(ByteUtil.int2Bytes(buff.length));
        bb.put(buff);
        return bb.array();
    }
}
