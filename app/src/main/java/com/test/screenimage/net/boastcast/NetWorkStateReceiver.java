package com.test.screenimage.net.boastcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.test.screenimage.utils.NetWorkUtils;
import com.test.screenimage.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by wt on 2018/7/2.
 * 监听网络变化
 */
public class NetWorkStateReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("网络状态发生变化");
        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService
                    (Context.CONNECTIVITY_SERVICE);
            //获取ConnectivityManager对象对应的NetworkInfo对象
            // 获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo ethNetInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected() || ethNetInfo != null && ethNetInfo.isConnected()) {
                EventBus.getDefault().post(true);
                return;
            }
            if (!wifiNetworkInfo.isConnected()&&!ethNetInfo.isConnected()) {
                Log.e("lw", "onReceive: "+ethNetInfo );
                EventBus.getDefault().post(false);
                ToastUtils.showShort(context, "请先连接网络！！");
                return;
            }
            return;
        }
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.
                CONNECTIVITY_SERVICE);
        //获取所有网络连接的信息
        Network[] networks = connMgr.getAllNetworks();
        //通过循环将网络信息逐个取出来
        for (int i = 0; i < networks.length; i++) {
            //获取ConnectivityManager对象对应的NetworkInfo对象
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI||networkInfo.getType()
                    ==ConnectivityManager.TYPE_ETHERNET) {
                if (networkInfo.isConnected()) {
                    EventBus.getDefault().post(true);
                } else if (!networkInfo.isConnected()) {
                    Log.e("lw", "onReceive: zzzzz" );
                    EventBus.getDefault().post(false);
//                    ToastUtils.showShort(context, "请先连接网络！！");
                }
                break;
            }
        }

    }
}
