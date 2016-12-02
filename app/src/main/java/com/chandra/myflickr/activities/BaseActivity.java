package com.chandra.myflickr.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.utils.DialogUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.ButterKnife;


public abstract class BaseActivity extends AppCompatActivity {

    private static final Logger logger = LoggerFactory.getLogger(BaseActivity.class.getSimpleName());

    private Dialog mLoadingDialog;

    protected abstract void setLayoutResource();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadingDialog = DialogUtils.createCustomDialogLoading(this);

        setLayoutResource();
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        dismissLoadingDialog();
        super.onPause();
    }

    //----------------------------------------------------------------------------------------------------------
    // Loading UI Helpers
    //----------------------------------------------------------------------------------------------------------

    /**
     * show dialog loading
     */
    public void showLoadingDialog() {
        if (mLoadingDialog != null && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();

            View viewContainer = mLoadingDialog.findViewById(R.id.loading_progress_wheel_view_container);
            if (viewContainer == null)
                return;

            Animation rotation = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.loading_animate);
            ImageView imgLoading = (ImageView) viewContainer.findViewById(R.id.iv_loading);
            viewContainer.setVisibility(View.VISIBLE);
            imgLoading.startAnimation(rotation);
            rotation.setDuration(800);
        }
    }

    /**
     * Dismiss loading dialog if it's showing
     */
    public void dismissLoadingDialog() {
        if (mLoadingDialog != null) {
            try {
                mLoadingDialog.dismiss();
            } catch (Exception e) {
                // dismiss dialog after destroy activity
            }
        }
    }


}
