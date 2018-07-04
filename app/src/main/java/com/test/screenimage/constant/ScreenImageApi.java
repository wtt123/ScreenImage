package com.test.screenimage.constant;

/**
 * Created by wt on 2018/6/11.
 * 设备常用配置
 */
public class ScreenImageApi {

    public static final byte encodeVersion1 = 0x00;       //版本号1

    public class RECORD {   //录屏指令
        public static final int MAIN_CMD = 0xA2; //录屏主指令
        public static final short RECORDER_REQUEST_START = 0x01; //投屏，请求开始
        public static final int Command_HostUpdated = 0x1C; //{IP}:{Name}|{IP}:{Name}…投屏者信息
        public static final int DATE_PALY = 0x01;//音视频解析播放
    }

    public class SERVER {//服务端与客户端交互指令
        public static final int MAIN_CMD = 0xA0; //投屏回传主指令
        public static final int INITIAL_SUCCESS = 0x01;//服务器初始化成功
    }

    public class LOGIC_REQUEST {    //客户端向服务器请求的指令
        public static final int MAIN_CMD = 112;
        public static final int GET_START_INFO = 1; //传输开始投屏需要的关键信息 例 1920,1080(长,宽)
    }

    public class LOGIC_REPONSE {    //返回给客户端的指令
        public static final int MAIN_CMD = 111;

        public static final int GET_START_INFO = 1; //传输开始投屏需要的关键信息 例 1920,1080(长,宽)

    }
}
