package com.chandra.myflickr.tasks;

import android.app.Activity;
import android.os.AsyncTask;

import com.chandra.myflickr.activities.LoginActivity;
import com.chandra.myflickr.flickr.FlickrManager;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;

import org.slf4j.LoggerFactory;


public class GetOAuthTokenTask extends AsyncTask<String, Integer, OAuth> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GetOAuthTokenTask.class.getSimpleName());
    private Activity mActivity;

    public GetOAuthTokenTask(Activity activity) {
        mActivity = activity;
    }

    @Override
    protected OAuth doInBackground(String... strings) {
        String oauthToken = strings[0];
        String oauthTokenSecret = strings[1];
        String oauthVerifier = strings[2];

        Flickr flickr = FlickrManager.getInstance().getFlickr();
        OAuthInterface oauthApi = flickr.getOAuthInterface();
        try {
            return oauthApi.getAccessToken(oauthToken, oauthTokenSecret, oauthVerifier);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(OAuth oAuth) {
        if (mActivity == null || !(mActivity instanceof LoginActivity))
            return;

        ((LoginActivity) mActivity).onOAuthDone(oAuth);
    }
}
