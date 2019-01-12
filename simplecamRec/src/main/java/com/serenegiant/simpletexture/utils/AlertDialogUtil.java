package com.serenegiant.simpletexture.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertDialogUtil {

    private Context mContext;
    private int layoutId;
    private String title;
    private String message;
    private String[] string;
    private String state;

    public AlertDialogUtil(Context context,int layoutId,String title,String message,String[] string,String state){
        this.mContext = context;
        this.layoutId = layoutId;
        this.title = title;
        this.message = message;
        this.string = string;
        this.state = state;
    }

    public static void showCommonDialog(Context context, String title, String message,
                                        DialogInterface.OnClickListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("确定",listener);

        builder.setNegativeButton("取消", listener);
        builder.show();

    }
}
