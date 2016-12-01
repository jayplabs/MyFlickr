package com.chandra.myflickr.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chandra.myflickr.R;
import com.chandra.myflickr.models.PhotoComment;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {


    private ArrayList<PhotoComment> mCommentsArray;
    private Context mContext;

    public CommentsRecyclerAdapter(Context context, ArrayList<PhotoComment> comments) {
        mContext = context;
        mCommentsArray = comments;
    }

    public void updateCommentsArray(ArrayList<PhotoComment> newCommentsArray) {
        mCommentsArray = newCommentsArray;
        notifyDataSetChanged();
    }

    public void addComment(PhotoComment comment) {
        mCommentsArray.add(comment);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position < 0 || position >= mCommentsArray.size())
            return;

        final PhotoComment comment = mCommentsArray.get(position);
        String author = "";
        String commentText = "";

        if (comment != null) {
            author = comment.getAuthorName();
            commentText = comment.getCommentData();
        }

        //Config
        holder.tvAuthor.setText(author);
        holder.tvComment.setText(commentText);
    }

    @Override
    public int getItemCount() {
        return mCommentsArray.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.authorIcon)
        ImageView ivAuthorIcon;

        @BindView(R.id.authorName)
        TextView tvAuthor;

        @BindView(R.id.authorComment)
        TextView tvComment;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
