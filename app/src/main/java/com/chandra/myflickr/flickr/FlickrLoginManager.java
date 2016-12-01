package com.chandra.myflickr.flickr;

import com.chandra.myflickr.managers.CacheManager;
import com.chandra.myflickr.utils.StringUtils;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import static com.chandra.myflickr.Constants.KEY_OAUTH_TOKEN;
import static com.chandra.myflickr.Constants.KEY_TOKEN_SECRET;
import static com.chandra.myflickr.Constants.KEY_USER_ID;
import static com.chandra.myflickr.Constants.KEY_USER_NAME;

public class FlickrLoginManager {


    public static boolean hasLogin() {
        OAuth oauth = getOAuthToken();
        if (oauth == null || oauth.getUser() == null)
            return false;
        return true;
    }

    public static OAuth getOAuthToken() {
        String oauthTokenString = CacheManager.getStringCacheData(KEY_OAUTH_TOKEN);
        String tokenSecret = CacheManager.getStringCacheData(KEY_TOKEN_SECRET);
        if (StringUtils.isNull(oauthTokenString) && StringUtils.isNull(tokenSecret)) {
            return null;
        }

        OAuth oauth = new OAuth();
        String userName = CacheManager.getStringCacheData(KEY_USER_NAME);
        String userId = CacheManager.getStringCacheData(KEY_USER_ID);
        if (StringUtils.isNotNull(userId)) {
            User user = new User();
            user.setUsername(userName);
            user.setId(userId);
            oauth.setUser(user);
        }

        OAuthToken oauthToken = new OAuthToken();
        oauth.setToken(oauthToken);
        oauthToken.setOauthToken(oauthTokenString);
        oauthToken.setOauthTokenSecret(tokenSecret);
        return oauth;
    }

    public static void saveOAuthToken(String userName, String userId, String token, String tokenSecret) {
        CacheManager.saveStringCacheData(KEY_OAUTH_TOKEN, token);
        CacheManager.saveStringCacheData(KEY_TOKEN_SECRET, tokenSecret);
        CacheManager.saveStringCacheData(KEY_USER_NAME, userName);
        CacheManager.saveStringCacheData(KEY_USER_ID, userId);
    }

    public static void clearOAuthData() {
        CacheManager.saveStringCacheData(KEY_OAUTH_TOKEN, null);
        CacheManager.saveStringCacheData(KEY_TOKEN_SECRET, null);
        CacheManager.saveStringCacheData(KEY_USER_NAME, null);
        CacheManager.saveStringCacheData(KEY_USER_ID, null);
    }

    public static String getUserName() {
        return CacheManager.getStringCacheData(KEY_USER_NAME);
    }
}
