package com.chandra.myflickr.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.activities.CommentsActivity;
import com.chandra.myflickr.flickr.cache.ImageDownloadTask;
import com.chandra.myflickr.models.FlickrPhoto;
import com.chandra.myflickr.utils.PhotoUtils;

import java.util.ArrayList;


public class PhotoViewerAdapter extends PagerAdapter {

    private Context mContext;
    private SparseArray<View> mViews;
    private ArrayList<FlickrPhoto> mDataArray;

    public PhotoViewerAdapter(Context context, ArrayList<FlickrPhoto> dataArray) {
        mContext = context;
        mDataArray = dataArray;
        mViews = new SparseArray<>();
    }

    @Override
    public int getCount() {
        if (mDataArray == null) {
            return 0;
        }
        return mDataArray.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public int getItemPosition(Object object) {
        View view = (View) object;
        return mDataArray.indexOf(view.getTag());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.remove(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_photo_viewer, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        View ivImageHolder = view.findViewById(R.id.image_default_holder);
        View ivProgress = view.findViewById(R.id.image_progress);
        ImageView ivComment = (ImageView) view.findViewById(R.id.iv_comment);
        TextView commentCounter = (TextView) view.findViewById(R.id.comment_counter);

        ivImageHolder.setVisibility(View.VISIBLE);
        ivProgress.setVisibility(View.VISIBLE);

        final FlickrPhoto photo = mDataArray.get(position);
        if (photo != null) {
            ImageDownloadTask task = new ImageDownloadTask(imageView);
            Drawable drawable = new PhotoUtils.DownloadedDrawable(task);
            imageView.setImageDrawable(drawable);
            task.execute(photo.getUrl());
        }

        int commentCount = photo.getCommentSum();
        commentCounter.setText(commentCount + " " + ((commentCount == 1) ? "comment" : "comments"));

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CommentsActivity.newInstance(mContext, photo, position);
                mContext.startActivity(intent);
            }
        });
        container.addView(view);
        view.setTag(photo);
        mViews.put(position, view);
        return view;
    }
}
