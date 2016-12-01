package com.chandra.myflickr.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import com.chandra.myflickr.Constants;
import com.chandra.myflickr.R;
import com.chandra.myflickr.flickr.FlickrLoginManager;
import com.chandra.myflickr.tasks.FlickrLoginTask;
import com.chandra.myflickr.tasks.GetOAuthTokenTask;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import butterknife.OnClick;

public class LoginActivity extends BaseActivity {

    private static final Logger logger = LoggerFactory.getLogger(LoginActivity.class.getSimpleName());

    private static final String EXTRACT = "extract-logout";

    public static Intent newInstance(Context context, boolean logout) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(EXTRACT, logout);
        return intent;
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        // this is very important, otherwise you would get a null Scheme in the onResume later on.
        // you have to set intent before it's being used in onResume
        logger.debug("On new intent : " + intent.getScheme());
        setIntent(intent);
    }

    @Override
    protected void setLayoutResource() {
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            return;

        //Check logout ==> clear cache
        boolean isLogout = bundle.getBoolean(EXTRACT);
        if (isLogout) {
            onClearOAuthentication();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        logger.debug("onResume");
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        logger.debug("Scheme :" + scheme);
        if (!Constants.CALLBACK_SCHEME.equals(scheme)) {
            return;
        }
        showLoadingDialog();
        onGetOAuthToken(intent);
    }

    //----------------------------------------------------------------------------------------------
    // Event
    //----------------------------------------------------------------------------------------------

    @OnClick(R.id.btn_flick_login)
    protected void onFlickrLoginClicked() {
        logger.debug("Login Clicked");
        if (FlickrLoginManager.hasLogin()) {
            performAfterLogin();
            return;
        }
        getAuthorization();
    }

    protected void getAuthorization() {
        logger.debug("Getting User Authorization");
        FlickrLoginTask flickrLoginTask = new FlickrLoginTask(this);
        flickrLoginTask.execute();
    }

    protected void performAfterLogin() {
        logger.debug("Show screen after login");
        Intent intent = PhotoGalleryActivity.newInstance(this);
        startActivity(intent);
        finish();
    }

    protected void onClearOAuthentication() {
        FlickrLoginManager.saveOAuthToken("", "", "", "");
        FlickrLoginManager.clearOAuthData();
    }

    protected void onGetOAuthToken(Intent intent) {
        logger.debug("getting oauth token");
        Uri uri = intent.getData();
        String query = uri.getQuery();
        String[] data = query.split("&");
        if (data == null || data.length != 2) {
            dismissLoadingDialog();
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
            return;
        }

        String oauthToken = data[0].substring(data[0].indexOf("=") + 1);
        String oauthVerifier = data[1].substring(data[1].indexOf("=") + 1);
        OAuth oauth = FlickrLoginManager.getOAuthToken();
        if (oauth == null || oauth.getToken() == null || oauth.getToken().getOauthTokenSecret() == null) {
            dismissLoadingDialog();
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
            return;
        }

        GetOAuthTokenTask task = new GetOAuthTokenTask(this);
        task.execute(oauthToken, oauth.getToken().getOauthTokenSecret(), oauthVerifier);
    }

    public void onOAuthDone(OAuth result) {
        if (result == null) {
            dismissLoadingDialog();
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
            return;
        }

        User user = result.getUser();
        if (user == null || user.getId() == null) {
            dismissLoadingDialog();
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
            return;
        }
        OAuthToken token = result.getToken();
        if (token == null || token.getOauthToken() == null || token.getOauthTokenSecret() == null) {
            dismissLoadingDialog();
            Toast.makeText(this, "Authorization failed", Toast.LENGTH_LONG).show();
            return;
        }

        //Debug
        String message = String.format(Locale.US, "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s",
                user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
        logger.debug(message);

        FlickrLoginManager.saveOAuthToken(user.getUsername(), user.getId(), token.getOauthToken(), token.getOauthTokenSecret());
        dismissLoadingDialog();
        performAfterLogin();
    }
}
