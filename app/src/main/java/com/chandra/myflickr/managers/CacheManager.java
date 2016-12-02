package com.chandra.myflickr.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.chandra.myflickr.utils.MyApplication;
import com.chandra.myflickr.utils.StringUtils;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.chandra.myflickr.misc.Constants.PREFS_NAME;


public class CacheManager {

    private static final String KEY_CREATED_TIMESTAMP = "_created_at";

    public static SharedPreferences getSharedPreferences() {
        Context ctx = MyApplication.getAppContext();
        if (ctx == null)
            return null;
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings;
    }

    //*************************************************************************
    // String
    //*************************************************************************

    public static void saveStringCacheData(final String key, String data) {
        if (key == null || data == null)
            return;

        SharedPreferences settings = getSharedPreferences();
        if (settings == null)
            return;
        SharedPreferences.Editor editor = settings.edit();
        if (editor == null)
            return;

        editor.putString(key, data);
        editor.apply();
    }

    public static String getStringCacheData(String key) {
        if (key == null)
            return null;

        SharedPreferences settings = getSharedPreferences();
        if (settings == null)
            return null;

        return settings.getString(key, null);
    }


    //*************************************************************************
    // List
    //*************************************************************************

    public static void saveListCacheData(final String key, ArrayList list) {
        saveObjectCacheData(key, list);
    }

    public static <T> ArrayList<T> getListCacheData(final String key, Type type) {
        if (key == null)
            return null;
        SharedPreferences settings = getSharedPreferences();
        if (settings == null)
            return null;

        String jsonString = settings.getString(key, null);
        if (jsonString == null || jsonString.length() <= 0)
            return null;

        Gson gson = new Gson();
        Object object = null;

        try {
            object = gson.fromJson(jsonString, type);

        } catch (Exception e) {
            String message = e.getMessage();
            object = null;
        }

        final ArrayList<T> finalObject = (ArrayList<T>)object;
        return finalObject;
    }

    //*************************************************************************
    // Object
    //*************************************************************************

    public static void saveObjectCacheData(final String key, Object object) {
        if (key == null || object == null)
            return;

        SharedPreferences settings = getSharedPreferences();
        if (settings == null)
            return;
        SharedPreferences.Editor editor = settings.edit();
        if (editor == null)
            return;

        //Serialize the list
        Gson mJson = new Gson();
        String jsonString = mJson.toJson(object);

        //Save it & the timestamp it was save
        if (StringUtils.isNotNull(jsonString)) {
            editor.putString(key, jsonString);
            editor.putLong(key + KEY_CREATED_TIMESTAMP, System.currentTimeMillis());
        }

        editor.apply();
    }
}
