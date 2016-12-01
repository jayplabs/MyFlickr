package com.chandra.myflickr.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.chandra.myflickr.MyApplication;
import com.chandra.myflickr.flickr.FlickrLoginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SplashActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(SplashActivity.class.getSimpleName());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FlickrLoginManager.hasLogin()) {
            logger.debug("User not logged in");
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
        logger.debug("Display Login Activity");
        Intent intent = LoginActivity.newInstance(MyApplication.getAppContext(), !doLogout);
        startActivity(intent);

        if (doLogout) {
            overridePendingTransition(0, 0);
        }
    }
}
