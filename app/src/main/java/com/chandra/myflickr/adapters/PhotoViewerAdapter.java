package com.chandra.myflickr.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.activities.CommentsActivity;
import com.chandra.myflickr.models.FlickrPhoto;
import com.facebook.drawee.view.SimpleDraweeView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class PhotoViewerAdapter extends PagerAdapter {

    Logger logger = LoggerFactory.getLogger(PhotoViewerAdapter.class.getSimpleName());

    private Context mContext;
    private SparseArray<View> mViews;
    private ArrayList<FlickrPhoto> mDataArray;
    private SimpleDraweeView mImageView;

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

        mImageView = (SimpleDraweeView) view.findViewById(R.id.image_view);
        ImageView ivComment = (ImageView) view.findViewById(R.id.iv_comment);
        TextView commentCounter = (TextView) view.findViewById(R.id.comment_counter);


        String imageUrl = "";
        final FlickrPhoto photo = mDataArray.get(position);
        if (photo != null) {
            imageUrl = photo.getUrl();
            loadImage(imageUrl);
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

    private void loadImage(String imageUrl) {
        mImageView.setImageURI(imageUrl);
    }
}
