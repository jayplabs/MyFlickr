package com.chandra.myflickr;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {

    private static final String TAG = "PhotoManager_Chandra";

    private static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return mContext;
    }
}
