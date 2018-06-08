package com.test.screenimage.utils;

import android.util.Log;

/**
 * 打印log
 */
public class SopCastLog {
    private static boolean open = true;

    public static void isOpen(boolean isOpen) {
        open = isOpen;
    }

    public static void d(String tag, String msg) {
        if(open) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if(open) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if(open) {
            Log.e(tag, msg);
        }
    }
}
