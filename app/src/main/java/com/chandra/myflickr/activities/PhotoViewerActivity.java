package com.chandra.myflickr.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.chandra.myflickr.R;
import com.chandra.myflickr.adapters.PhotoViewerAdapter;
import com.chandra.myflickr.models.FlickrPhoto;

import java.util.ArrayList;

import butterknife.BindView;

public class PhotoViewerActivity extends BaseActivity {

    private static final String EXTRACT_DATA = "data-extract";
    private static final String EXTRACT_POSITION = "position-extract";

    protected ArrayList<FlickrPhoto> mDataArray;
    protected PhotoViewerAdapter mAdapter;
    protected int position;

    @BindView(R.id.vp_images_slider)
    ViewPager mViewPager;

    @Override
    protected void setLayoutResource() {
        setContentView(R.layout.activity_photoviewer);
    }

    public static Intent newInstance(Context context, ArrayList<FlickrPhoto> mDataArray, int position) {
        Intent intent = new Intent(context, PhotoViewerActivity.class);
        intent.putExtra(EXTRACT_DATA, mDataArray);
        intent.putExtra(EXTRACT_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiseUI();
    }


    protected void initialiseUI() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null)
            finish();
        position = bundle.getInt(EXTRACT_POSITION);
        mDataArray = (ArrayList<FlickrPhoto>) bundle.getSerializable(EXTRACT_DATA);

        mAdapter = new PhotoViewerAdapter(this, mDataArray);

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(position);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
