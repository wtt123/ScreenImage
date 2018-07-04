package com.test.screenimage;

import android.app.Application;
import android.os.Handler;

import com.test.screenimage.utils.SupportMultipleScreensUtil;


/**
 * Created by wt on 2018/5/28.
 */

public class MyApplication extends Application {
    public static Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(getMainLooper());
        SupportMultipleScreensUtil.init(this);
    }
}
