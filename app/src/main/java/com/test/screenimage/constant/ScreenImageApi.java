package com.test.screenimage.constant;

/**
 * Created by wt on 2018/6/11.
 * 设备常用配置
 */
public class ScreenImageApi {
    public static final byte encodeVersion1 = 0x00;       //版本号1
    public class RECORD {   //录屏指令
        public static final int MAIN_CMD = 0xA2; //主指令
        public static final int Command_HostUpdated = 0x1C; //{IP}:{Name}|{IP}:{Name}…投屏者信息
    }
}
