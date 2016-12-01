package com.chandra.myflickr.tasks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.chandra.myflickr.Constants;
import com.chandra.myflickr.activities.LoginActivity;
import com.chandra.myflickr.flickr.FlickrLoginManager;
import com.chandra.myflickr.flickr.FlickrManager;
import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

import java.net.URL;


public class FlickrLoginTask extends AsyncTask<Void, Integer, String> {

    private static final Uri OAUTH_CALLBACK_URI = Uri.parse(Constants.CALLBACK_SCHEME + "://oauth");
    private ProgressDialog mProgressDialog;
    private Context mContext;

    private LoginActivity mActivity;

    public FlickrLoginTask(Context context) {
        super();
        mContext = context;
        mActivity = (LoginActivity) context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext, AlertDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setTitle("");
        mProgressDialog.setMessage("Generating the authorization request...");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dlg) {
                FlickrLoginTask.this.cancel(true);
            }
        });

        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        try {

            Flickr flickr = FlickrManager.getInstance().getFlickr();
            if (flickr == null) {
                return null;
            }

            OAuthToken oauthToken = flickr.getOAuthInterface().getRequestToken(OAUTH_CALLBACK_URI.toString());
            saveCurrentToken(oauthToken.getOauthTokenSecret());
            URL oauthUrl = flickr.getOAuthInterface().buildAuthenticationUrl(Permission.WRITE, oauthToken);
            return oauthUrl.toString();

        } catch (Exception e) {
            Log.e("Error to oauth", e.getMessage()); //$NON-NLS-1$
            return "error:" + e.getMessage(); //$NON-NLS-1$
        }
    }

    private void saveCurrentToken(String tokenSecret) {
        FlickrLoginManager.saveOAuthToken(null, null, null, tokenSecret);
    }

    @Override
    protected void onPostExecute(String oauthUrl) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        if (oauthUrl == null || oauthUrl.startsWith("error")) {
            Toast.makeText(mContext, oauthUrl, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }
}
