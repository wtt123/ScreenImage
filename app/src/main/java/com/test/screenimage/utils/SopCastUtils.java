package com.test.screenimage.utils;

import android.os.Build;
import android.os.Looper;


public class SopCastUtils {

    public interface INotUIProcessor {
        void process();
    }

    public static void processNotUI(final INotUIProcessor processor) {
        if(Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    processor.process();
                }
            }).start();
        } else {
            processor.process();
        }
    }

    public static boolean isOverLOLLIPOP() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
