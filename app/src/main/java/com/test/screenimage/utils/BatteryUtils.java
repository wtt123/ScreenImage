package com.test.screenimage.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by wt
 * Date on  2017/12/13 15:20:46.
 *
 * @Desc 检查设置忽略电池优化.
 */

public class BatteryUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void ignoreBatteryOptimization(Context context) {
        PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(POWER_SERVICE);
        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        }
    }
}
