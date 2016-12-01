package com.chandra.myflickr.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import com.chandra.myflickr.R;


public class DialogUtils {

    // create dialog with custom layout
    public static Dialog createCustomDialogLoading(Activity activity) {
        Dialog dialog = new Dialog(activity, R.style.CustomDialogLoading);
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_progress_wheel, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);

        return dialog;
    }
}
