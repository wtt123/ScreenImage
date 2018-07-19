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
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取ConnectivityManager对象对应的NetworkInfo对象
            // 获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo ethNetInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if (wifiNetworkInfo == null && ethNetInfo == null) {
                EventBus.getDefault().post("");
                ToastUtils.showShort(context, "请先连接网络！！");
            } else if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
                EventBus.getDefault().post(NetWorkUtils.getLocalIpAddress());
                ToastUtils.showShort(context, "已连接无线网！！");
            } else if (ethNetInfo != null && ethNetInfo.isConnected()) {
                EventBus.getDefault().post(NetWorkUtils.getLocalIpAddress());
                ToastUtils.showShort(context, "已连接有线网！！");
            } else {
                EventBus.getDefault().post("");
                ToastUtils.showShort(context, "请先连接网络！！");
            }
            return;
        }
        //API大于23时使用下面的方式进行网络监听
        //获得ConnectivityManager对象
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取所有网络连接的信息
        Network[] networks = connMgr.getAllNetworks();
        //用于存放网络连接信息
        StringBuilder sb = new StringBuilder();
        //通过循环将网络信息逐个取出来
        if (networks.length == 0) {
            EventBus.getDefault().post("");
            ToastUtils.showShort(context, "请先连接网络！！");
            return;
        }
        for (int i = 0; i < networks.length; i++) {
            //获取ConnectivityManager对象对应的NetworkInfo对象
            NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
            if (networkInfo.isConnected()) {
                EventBus.getDefault().post(NetWorkUtils.getLocalIpAddress());
            } else {
                EventBus.getDefault().post("");
            }
        }
    }
}
