package com.test.screenimage;

import android.app.Application;

import com.test.screenimage.utils.SupportMultipleScreensUtil;


/**
 * Created by wt on 2018/5/28.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SupportMultipleScreensUtil.init(this);
    }
}
