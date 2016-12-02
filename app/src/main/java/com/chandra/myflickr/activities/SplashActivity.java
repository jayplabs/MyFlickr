package com.chandra.myflickr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.chandra.myflickr.utils.MyApplication;
import com.chandra.myflickr.managers.FlickrLoginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(SplashActivity.class.getSimpleName());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FlickrLoginManager.hasLogin()) {
            showLoginActivity(true);
        } else {
            Intent intent = PhotoGalleryActivity.newInstance(MyApplication.getAppContext());
            startActivity(intent);
        }
        SplashActivity.this.finish();
    }

    //----------------------------------------------------------------------------------------------
    // Event
    //----------------------------------------------------------------------------------------------
    protected void showLoginActivity(boolean doLogout) {
        Intent intent = LoginActivity.newInstance(MyApplication.getAppContext(), !doLogout);
        startActivity(intent);

        if (doLogout) {
            overridePendingTransition(0, 0);
        }
    }
}
