package com.chandra.myflickr.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.activities.CommentsActivity;
import com.chandra.myflickr.models.FlickrPhoto;
import com.chandra.myflickr.utils.DeviceUtils;
import com.facebook.drawee.view.SimpleDraweeView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    private final Logger logger = LoggerFactory.getLogger(PhotosAdapter.class.getSimpleName());
    private static int imageHeight = 0;

    private OnPhotoItemClickListener listener;
    private ArrayList<FlickrPhoto> mDataArray;
    private Context mContext;

    public PhotosAdapter(Context context, ArrayList<FlickrPhoto> data) {
        mContext = context;
        mDataArray = data;
        listener = (OnPhotoItemClickListener) context;
        imageHeight = DeviceUtils.getDeviceScreenHeight(context) / 3;
    }

    public void updateDataArray(ArrayList<FlickrPhoto> newDataArray) {
        this.mDataArray = newDataArray;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (position < 0 || position >= mDataArray.size())
            return;

        final FlickrPhoto photo = mDataArray.get(position);
        String imageUrl = "";
        int commentSum = 0;

        if (photo != null) {
            imageUrl = photo.getUrl();
            commentSum = photo.getCommentSum();
        }

        //Config
        ViewGroup.LayoutParams params = holder.ivPhoto.getLayoutParams();
        params.height = imageHeight;
        holder.ivPhoto.setLayoutParams(params);

        //Setting
        holder.commentCounter.setText(String.valueOf(commentSum));

        loadImage(holder, imageUrl);

        //Event
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommentClicked(holder, photo, position);
            }
        });


        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPhotoItemClicked(position);
            }
        });
    }

    private void loadImage(final ViewHolder holder, String imageUrl) {

        holder.ivPhoto.setImageURI(imageUrl);
    }

    protected void onCommentClicked(final ViewHolder holder, final FlickrPhoto photo, final int position) {
        Intent intent = CommentsActivity.newInstance(mContext, photo, position);
        mContext.startActivity(intent);

    }

    @Override
    public int getItemCount() {
        if (mDataArray == null)
            return 0;
        return mDataArray.size();
    }

    public interface OnPhotoItemClickListener {
        void onPhotoItemClicked(int position);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_view)
        SimpleDraweeView ivPhoto;

        @BindView(R.id.iv_comment)
        ImageView btnComment;

        @BindView(R.id.comment_counter)
        TextView commentCounter;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
