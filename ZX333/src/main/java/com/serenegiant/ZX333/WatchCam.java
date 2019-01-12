package com.serenegiant.ZX333;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/26.
 */
public class WatchCam extends Service {
    private static final String TAG = "WatchCam";
    private static final boolean DEBUG = true;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "start onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
