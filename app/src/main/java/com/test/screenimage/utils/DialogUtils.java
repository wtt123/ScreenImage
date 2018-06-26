package com.test.screenimage.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

/**
 * Created by wt on 2018/6/25.
 * 弹出框工具类
 */
public class DialogUtils {

    private static DialogUtils instance = null;

    public static DialogUtils getInstance() {
        if (instance == null) {
            instance = new DialogUtils();
        }
        return instance;
    }

    public void showDialog(Context context, String titleInfo,String message,String postBtnName,
                           String navBtnName,
                           final DialogUtils.DialogCallBack callBack) {
        AlertDialog.Builder alterDialog = new AlertDialog.Builder(context);
        alterDialog.setTitle(titleInfo);
        alterDialog.setMessage(message);
        alterDialog.setCancelable(true);

        alterDialog.setPositiveButton(postBtnName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.exectEvent();
            }
        });
        alterDialog.setNegativeButton(navBtnName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alterDialog.show();
    }

    public interface DialogCallBack {
        void exectEvent();
    }

}
